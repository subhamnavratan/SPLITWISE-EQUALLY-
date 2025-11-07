async function login() {
    const identifier = document.getElementById("identifier").value;
    const password = document.getElementById("password").value;

    const res = await fetch("http://localhost:8080/api/users/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({ identifier, password })
    });

    if (!res.ok) {
        alert("Invalid login!");
        return;
    }

    const user = await res.json();
    localStorage.setItem("phone", user.phone);
    localStorage.setItem("name", user.name);

    window.location.href = "dashboard.html";
}
