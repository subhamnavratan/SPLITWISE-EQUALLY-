const phone = localStorage.getItem("phone");

loadGroups();

/* Load groups */
async function loadGroups() {
    const res = await fetch(`http://localhost:8080/groups/phone/${phone}`);
    const groups = await res.json();

    const list = document.getElementById("groupList");
    list.innerHTML = "";

    groups.forEach(g => {
        const div = document.createElement("div");
        div.className = "group-item";

        div.innerHTML = `
            <span>${g.groupName}</span>
            <button class="open-btn" onclick="openGroup('${g.groupName}', ${g.creatorPhone})">Open</button>
        `;

        list.appendChild(div);
    });
}

/* ✅ OPEN GROUP PAGE ✅ */
function openGroup(name, creator) {
    console.log("Opening group:", name, "Creator:", creator);

    // Store the values
    localStorage.setItem("groupName", name);
    localStorage.setItem("creatorPhone", creator);

    // Redirect
    window.location.href = "group.html";
}

/* Modal */
function openCreateModal() {
    document.getElementById("modal").style.display = "flex";
}

function closeModal() {
    document.getElementById("modal").style.display = "none";
}

/* Create group */
async function createGroup() {
    const name = document.getElementById("groupNameInput").value.trim();
    if (name === "") {
        alert("Enter group name!");
        return;
    }

    const res = await fetch("http://localhost:8080/groups/createByPhone", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            groupName: name,
            creatorPhone: Number(phone)
        })
    });

    if (res.ok) {
        alert("Group created!");
        closeModal();
        loadGroups();
    } else {
        alert("Failed to create group");
    }
}
