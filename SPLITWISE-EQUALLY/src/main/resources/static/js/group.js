const groupName = localStorage.getItem("groupName");
const creatorPhone = localStorage.getItem("creatorPhone");
const myPhone = localStorage.getItem("phone");

document.getElementById("groupTitle").innerText = groupName;

// Hide delete button if not creator
if (creatorPhone != myPhone) {
    document.getElementById("deleteGroupBtn").style.display = "none";
}

/* ---------------------------------------------------
   LOAD GROUP MEMBERS WITH HISTORY
---------------------------------------------------- */
async function loadGroupMembers() {
    const res = await fetch(`http://localhost:8080/members/${groupName}`);
    const members = await res.json();

    const container = document.getElementById("membersArea");
    container.innerHTML = "";

    for (let m of members) {
        // ✅ Fetch phone for this userId
        const phone = await getPhoneFromUserId(m.userId);

        const div = document.createElement("div");
        div.className = "member-card";

        div.innerHTML = `
            <h3>${m.name}</h3>
            <p><b>Total Paid:</b> ₹ ${m.totalPaidAmount}</p>

            <div class="transaction-history">
                <b>Payment History:</b><br>
                ${m.detail.length === 0 ?
                    "<p>No payments yet</p>" :
                    m.detail.map(d => `
                        <div class="transaction-item">
                            ₹${d.amount} — ${d.description}
                        </div>
                    `).join('')
                }
            </div>

            <div class="add-pay-row">
                <input type="number" id="amount-${phone}" placeholder="Amount">
                <input type="text" id="desc-${phone}" placeholder="Description">
                <button class="add-btn" onclick="addPayment('${phone}')">Add</button>
            </div>
        `;

        container.appendChild(div);
    }
}

loadGroupMembers();

/* ---------------------------------------------------
   FETCH PHONE FROM BACKEND USING userId
---------------------------------------------------- */
async function getPhoneFromUserId(userId) {
    const res = await fetch(`http://localhost:8080/api/users/id/${userId}`);
    const user = await res.json();
    return user.phone;  // ✅ Phone returned directly
}

/* ---------------------------------------------------
   ADD PAYMENT
---------------------------------------------------- */
async function addPayment(phone) {
    const amount = document.getElementById(`amount-${phone}`).value;
    const description = document.getElementById(`desc-${phone}`).value;

    const res = await fetch(`http://localhost:8080/members/paymentByPhone/${groupName}/${phone}`, {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ amount, description })
    });

    if (res.ok) {
        alert("Payment added");
        loadGroupMembers();
    } else {
        alert("Failed to add payment");
    }
}

/* ---------------------------------------------------
   TOTAL AMOUNT
---------------------------------------------------- */
async function getTotal() {
    const res = await fetch(`http://localhost:8080/calculate/total/${groupName}`);
    const total = await res.text();

    document.getElementById("summaryOutput").innerHTML =
        `<b>Total Group Amount:</b> ₹ ${total}`;
}

/* ---------------------------------------------------
   SIMPLIFY SETTLEMENT
---------------------------------------------------- */
async function simplify() {
    const res = await fetch(`http://localhost:8080/calculate/settle/${groupName}`);
    const transactions = await res.json();

    if (transactions.length === 0) {
        document.getElementById("summaryOutput").innerHTML =
            "All balances are already settled ✅";
        return;
    }

    let html = "<b>Suggested Settlement:</b><br><br>";
    transactions.forEach(t => {
        html += `${t.payerName} → ${t.receiverName}: ₹${t.amount}<br>`;
    });

    document.getElementById("summaryOutput").innerHTML = html;
}

/* ---------------------------------------------------
   DELETE GROUP
---------------------------------------------------- */
async function deleteGroup() {
    if (!confirm("Delete this entire group?")) return;

    const res = await fetch(`http://localhost:8080/groups/delete/${groupName}`, {
        method: "DELETE"
    });

    if (res.ok) {
        alert("Group deleted!");
        window.location.href = "dashboard.html";
    } else {
        alert("Failed to delete group");
    }
}
async function addMember() {
    const phone = document.getElementById("newMemberPhone").value.trim();

    if (!phone || phone.length !== 10) {
        alert("Enter valid 10 digit phone number");
        return;
    }

    const res = await fetch(`http://localhost:8080/groups/addByPhone/${groupName}/${phone}`, {
        method: "POST"
    });

    if (res.ok) {
        alert("Member added successfully ✅");
        document.getElementById("newMemberPhone").value = "";
        loadGroupMembers();
    } else {
        alert("Failed to add member ❌");
    }
}
async function removeMember() {
    const phone = document.getElementById("removeMemberPhone").value.trim();

    if (!phone) {
        alert("Enter 10 digit phone number");
        return;
    }

    if (!confirm("Are you sure you want to remove this member?")) return;

    const res = await fetch(`http://localhost:8080/groups/removeByPhone/${groupName}/${phone}`, {
        method: "DELETE"
    });

    if (res.ok) {
        alert("Member removed ✅");
        document.getElementById("removeMemberPhone").value = "";
        loadGroupMembers();
    } else {
        alert("Failed to remove member ❌");
    }
}

