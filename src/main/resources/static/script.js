/* ======================================================
   SecureLogX – SOC Dashboard Script
   ====================================================== */

const BASE = "http://localhost:8080";

document.addEventListener("DOMContentLoaded", () => {

    /* --------------------------------------------------
       HARD AUTH GUARD (NO DASHBOARD FLASH)
       -------------------------------------------------- */
    document.body.style.display = "none";

    fetch(`${BASE}/auth/me`, { credentials: "include" })
        .then(res => {
            if (!res.ok) throw new Error("Not authenticated");
            return res.json();
        })
        .then(r => {
            const analystInfo = document.getElementById("analystInfo");
            if (analystInfo) {
                analystInfo.textContent = "Analyst: " + r.data;
            }

            // ✅ Auth success → show dashboard
            document.body.style.display = "block";
        })
        .catch(() => {
            window.location.replace("login.html");
        });

    /* --------------------------------------------------
       LOGOUT
       -------------------------------------------------- */
    const logoutBtn = document.getElementById("logoutBtn");
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            fetch(`${BASE}/auth/logout`, {
                method: "POST",
                credentials: "include"
            }).finally(() => {
                window.location.replace("login.html");
            });
        });
    }

    /* --------------------------------------------------
       LOG ANALYSIS (PLACEHOLDER – SAFE)
       -------------------------------------------------- */
    const analyzeBtn = document.getElementById("analyzeBtn");
    if (analyzeBtn) {
        analyzeBtn.addEventListener("click", () => {
            alert("Log analysis hook ready (backend integration next).");
        });
    }

    /* --------------------------------------------------
       INCIDENT MODAL HANDLING
       -------------------------------------------------- */
    const closeIncidentBtn = document.getElementById("closeIncidentBtn");
    if (closeIncidentBtn) {
        closeIncidentBtn.addEventListener("click", () => {
            const modal = document.getElementById("incidentModal");
            if (modal) modal.classList.add("hidden");
        });
    }

});

