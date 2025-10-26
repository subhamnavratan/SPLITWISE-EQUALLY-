// signup.js

const API_BASE_URL = "http://localhost:8080";

function signup() {
    const name = document.getElementById('name').value.trim();
    const email = document.getElementById('email').value.trim();
    const phone = document.getElementById('phone').value.trim();
    const msg = document.getElementById('signup-msg');

    if (!name || !email || !phone) {
        msg.innerText = "All fields are required.";
        msg.style.color = "red";
        return;
    }

    if (!email.toLowerCase().endsWith("@gmail.com")) {
        msg.innerText = "Email must end with @gmail.com.";
        msg.style.color = "red";
        return;
    }

    fetch(`${API_BASE_URL}/api/users/register`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            name: name,
            phone: parseInt(phone), // Backend expects Long/number
            email: email
        })
    })
    .then(response => {
        if (response.ok) {
            msg.innerText = "Signup successful. Redirecting to login...";
            msg.style.color = "green";
            setTimeout(() => {
                window.location.href = "login.html";
            }, 2000);
        } else {
            // Read specific error message from the backend
            return response.json().then(data => {
                throw new Error(data.message || "Registration failed.");
            });
        }
    })
    .catch(error => {
        msg.innerText = `Signup failed: ${error.message}`;
        msg.style.color = "red";
        console.error("Signup error:", error);
    });
}