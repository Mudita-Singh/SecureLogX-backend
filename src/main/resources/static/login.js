const BASE = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("login-form");
    const status = document.getElementById("status");
    const button = document.querySelector("button.primary");

    if (!form) {
        console.error("Login form not found");
        return;
    }

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();

        status.textContent = "";
        status.className = "status";

        if (!username || !password) {
            status.textContent = "Username and password are required.";
            status.classList.add("error");
            return;
        }

        button.disabled = true;
        status.textContent = "Authenticating…";

        try {
            const response = await fetch(`${BASE}/auth/login`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Invalid credentials");
            }

            window.location.href = "index.html";
        } catch (err) {
            status.textContent = err.message || "Login failed.";
            status.classList.add("error");
        } finally {
            button.disabled = false;
        }
    });
});
