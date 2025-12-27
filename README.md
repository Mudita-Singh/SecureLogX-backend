# SecureLogX – Backend (Spring Boot)

SecureLogX Backend is a Spring Boot application that provides REST APIs for analyzing system log files and securely handling incident reports.

It extends the original CLI version by exposing the functionality through APIs so that it can later be used by a web-based user interface.

---

# What is the purpose of this backend?

The backend is responsible for:

- Analyzing system log files
- Detecting suspicious login behavior
- Generating security incident reports
- Encrypting generated reports
- Allowing secure decryption of reports through APIs

This separation makes the system easier to scale and integrate with a frontend.

---

# What can the backend do right now?

The backend currently supports:

- Reading log files from the file system
- Detecting repeated failed login attempts (e.g., SSH failures)
- Grouping suspicious activity by IP address
- Generating incident reports in JSON format
- Encrypting reports using a password
- Decrypting encrypted reports using the correct password
- Returning clean and structured API responses

---

# How does it work (simple flow)?

1. A client sends a log file path and password to the `/analyze` API.
2. The backend reads and analyzes the log file.
3. Suspicious login activity is detected and converted into incidents.
4. A JSON report is generated.
5. The report is encrypted and saved.
6. The encrypted file path is returned to the client.
7. The `/decrypt` API can be used to decrypt the report when needed.


