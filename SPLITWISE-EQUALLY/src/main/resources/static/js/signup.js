async function signup() {
    const user = {
        name: document.getElementById("name").value,
        phone: document.getElementById("phone").value,
        email: document.getElementById("email").value,
        password: document.getElementById("password").value
    };

    const res = await fetch("http://localhost:8080/api/users/register", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify(user)
    });

    if (res.ok) {
        alert("Signup successful!");
        window.location.href = "index.html";
    } else {
        alert("Signup failed.");
    }
}
