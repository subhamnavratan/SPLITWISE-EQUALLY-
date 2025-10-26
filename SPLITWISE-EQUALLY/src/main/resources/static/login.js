// login.js

const API_BASE_URL = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("login-form").addEventListener("submit", function (e) {
        e.preventDefault();

        const identifier = document.getElementById("identifier").value.trim();
        const msg = document.getElementById("login-msg");
        msg.textContent = "";

        if (!identifier) {
            msg.textContent = "Please enter your phone number or email.";
            msg.style.color = "red";
            return;
        }

        fetch(`${API_BASE_URL}/api/users/login/${identifier}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                } else {
                    // Check for 404/401 and throw a clear message
                    throw new Error("Login failed. User not found or incorrect credentials.");
                }
            })
            .then(user => {
                // SUCCESS: Store the user's ID, name, AND CRITICALLY, THE PHONE NUMBER.
                localStorage.setItem("loggedInUserId", user.userId);
                localStorage.setItem("loggedInUserName", user.name);
                localStorage.setItem("loggedInPhone", user.phone); // <--- ADDED THIS LINE

                // Redirect to group page
                window.location.href = "group.html";
            })
            .catch(error => {
                console.error("Login failed:", error);
                msg.textContent = error.message || "Error connecting to server. Check backend status.";
                msg.style.color = "red";
            });
    });
});