const BASE = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("signup-form");
    const status = document.getElementById("status");
    const button = document.querySelector("button.primary");

    if (!form) {
        console.error("Signup form not found");
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
        status.textContent = "Creating account…";

        try {
            const response = await fetch(`${BASE}/auth/signup`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (!response.ok) {
                throw new Error(data.message || "Signup failed");
            }

            // ✅ Auto-login expected from backend
            window.location.href = "index.html";
        } catch (err) {
            status.textContent = err.message || "Unable to create account.";
            status.classList.add("error");
        } finally {
            button.disabled = false;
        }
    });
});
