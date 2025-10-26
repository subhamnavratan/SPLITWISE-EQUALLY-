// group.js - Improved for Phone-Based API

const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    // CRITICAL: Get both ID (for security/group item processing) and PHONE (for API lookup/creation)
    const userId = localStorage.getItem("loggedInUserId");
    const userPhone = localStorage.getItem("loggedInPhone");

    if (!userId || !userPhone) {
        // Redirect if session data is incomplete
        window.location.href = "login.html";
        return;
    }

    const groupList = document.getElementById("group-list");
    const groupForm = document.getElementById("create-group-form");
    const groupMsg = document.getElementById("group-msg");

    // Load groups using the new phone endpoint
    function loadGroups() {
        // FIX: Using the user-friendly phone number API for lookup
        fetch(`${API_BASE_URL}/groups/phone/${userPhone}`)
            .then(res => {
                if (res.status === 404) return []; // User has no groups, return empty array
                if (!res.ok) throw new Error("Network response was not ok.");
                return res.json();
            })
            .then(groups => {
                groupList.innerHTML = "";
                if (groups.length === 0) {
                    groupList.innerHTML = "<p class='no-groups'>No groups found. Create one below.</p>";
                    return;
                }

                groups.forEach(group => {
                    const div = document.createElement("div");
                    div.className = "group-item";
                    div.textContent = group.groupName;
                    div.addEventListener("click", () => {
                        localStorage.setItem("currentGroup", group.groupName);
                        window.location.href = "members.html";
                    });
                    groupList.appendChild(div);
                });
            })
            .catch(err => {
                groupMsg.textContent = `Error loading groups: ${err.message}`;
                groupMsg.style.color = "red";
                console.error(err);
            });
    }

    // Create group
    groupForm.addEventListener("submit", function (e) {
        e.preventDefault();
        const groupNameInput = document.getElementById("new-group-name");
        const groupName = groupNameInput.value.trim();

        if (!groupName) return;

        // FIX: Using the new /createByPhone endpoint and the stored phone number
        fetch(`${API_BASE_URL}/groups/createByPhone`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                groupName: groupName,
                creatorPhone: parseInt(userPhone) // Use the phone number
            })
        })
        .then(res => {
            if (!res.ok) {
                // Read specific error message from the backend
                return res.json().then(data => {
                    throw new Error(data.message || "Failed to create group.");
                });
            }
            return res.json();
        })
        .then(() => {
            groupNameInput.value = ""; // Clear input
            groupMsg.textContent = `Group "${groupName}" created successfully!`;
            groupMsg.style.color = "green";
            loadGroups(); // Refresh to show new group
        })
        .catch(err => {
            groupMsg.textContent = `Failed to create group: ${err.message}`;
            groupMsg.style.color = "red";
            console.error(err);
        });
    });

    loadGroups(); // Initial load
});