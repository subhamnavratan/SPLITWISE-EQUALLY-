// members.js

const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
    const userId = localStorage.getItem("loggedInUserId");
    const userName = localStorage.getItem("loggedInUserName");
    const groupName = localStorage.getItem("currentGroup");

    if (!userId || !groupName) {
        alert("Please log in and select a group.");
        window.location.href = "login.html";
        return;
    }

    // Update UI headers
    document.getElementById("group-title").textContent = `Group: ${groupName}`;
    document.getElementById("group-name-display").textContent = `${groupName} (Logged in as: ${userName})`;

    const membersTableBody = document.querySelector("#membersTable tbody");
    const totalDisplay = document.getElementById("totalDisplay");
    const averageDisplay = document.getElementById("averageDisplay");
    const deleteGroupBtn = document.getElementById("deleteGroupBtn");
    const settlementDiv = document.getElementById("settlement-transactions");

    // Load all data (members, total, settlements)
    async function loadAllData() {
        // 1. Load Members and Balances
        try {
            const balancesResponse = await fetch(`${API_BASE_URL}/calculate/balances/${groupName}`);
            if (!balancesResponse.ok) throw new Error("Failed to load balances.");
            const members = await balancesResponse.json();

            membersTableBody.innerHTML = "";
            members.forEach(member => {
                const row = document.createElement("tr");
                const isCurrentUser = member.userId === userId;

                row.innerHTML = `
                    <td>${member.userId}</td>
                    <td>${member.name} ${isCurrentUser ? '(You)' : ''}</td>
                    <td class="${member.netBalance > 0 ? 'text-green' : member.netBalance < 0 ? 'text-red' : ''}">
                        ₹${member.netBalance}
                    </td>
                    <td>
                        <button onclick="addPayment('${member.userId}')" class="btn-pay">Add Payment</button>
                        </td>
                `;
                membersTableBody.appendChild(row);
            });
        } catch (err) {
            console.error(err);
            membersTableBody.innerHTML = `<tr><td colspan="4" class="text-error">${err.message}</td></tr>`;
        }

        // 2. Load Summary (Total & Average)
        try {
            const totalRes = await fetch(`${API_BASE_URL}/calculate/total/${groupName}`);
            const avgRes = await fetch(`${API_BASE_URL}/calculate/average/${groupName}`);

            const total = await totalRes.json();
            const avg = await avgRes.json();

            totalDisplay.textContent = `Total Spent: ₹${total}`;
            averageDisplay.textContent = `Share Per Person: ₹${avg.toFixed(2)}`;
        } catch (err) {
            console.error("Summary error:", err);
            totalDisplay.textContent = `Total Spent: ERROR`;
        }

        // 3. Load Settlement Plan
        try {
            const settlementRes = await fetch(`${API_BASE_URL}/calculate/settle/${groupName}`);
            if (!settlementRes.ok) throw new Error("Failed to load settlement plan.");
            const transactions = await settlementRes.json();

            settlementDiv.innerHTML = "";
            if (transactions.length === 0) {
                settlementDiv.innerHTML = "<p class='settled'>Group is fully settled! 🎉</p>";
            } else {
                transactions.forEach(tx => {
                    const p = document.createElement("p");
                    p.innerHTML = `<span class="payer">${tx.payerName}</span> **OWES** <span class="receiver">${tx.receiverName}</span>: <span class="amount">₹${tx.amount}</span>`;
                    settlementDiv.appendChild(p);
                });
            }
        } catch (err) {
            console.error("Settlement error:", err);
            settlementDiv.innerHTML = `<p class="text-error">Error calculating settlement: ${err.message}</p>`;
        }
    }

    // Add member (new API: /groups/add/{groupName}/{userId})
    window.addMember = async function () {
        const newUserId = document.getElementById("addUserId").value.trim();
        if (!newUserId) {
            alert("User ID is required");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/groups/add/${groupName}/${newUserId}`, {
                method: "POST"
            });

            if (!response.ok) throw new Error("Failed to add member. Check User ID.");

            alert("Member added and synced!");
            loadAllData();
        } catch (err) {
            console.error(err);
            alert("Error: " + err.message);
        }
    };

    // Add new payment (new API: /members/payment/{groupName}/{userId})
    window.addPayment = function (paymentUserId) {
        const amountStr = prompt("Enter payment amount (I paid):");
        const description = prompt("Enter description:");

        if (!amountStr || !description) {
            alert("Both fields are required.");
            return;
        }

        const amount = parseInt(amountStr);
        if (isNaN(amount) || amount <= 0) {
            alert("Please enter a valid amount.");
            return;
        }

        const payload = { amount, description };

        fetch(`${API_BASE_URL}/members/payment/${groupName}/${paymentUserId}`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        })
            .then(res => {
                if (!res.ok) throw new Error("Failed to add payment. Check server logs.");
                return res.text();
            })
            .then(() => {
                alert("Payment added successfully!");
                loadAllData();
            })
            .catch(err => alert(err.message));
    };

    // Delete entire group
    deleteGroupBtn.addEventListener("click", () => {
        if (confirm("Are you sure you want to delete this group and all its members? This action is permanent.")) {
            fetch(`${API_BASE_URL}/groups/delete/${groupName}`, { method: "DELETE" })
                .then(res => {
                    if (!res.ok) throw new Error("Group delete failed.");
                    alert("Group deleted successfully.");
                    window.location.href = "group.html";
                })
                .catch(err => alert("Error: " + err.message));
        }
    });

    // Initial load
    loadAllData();
});