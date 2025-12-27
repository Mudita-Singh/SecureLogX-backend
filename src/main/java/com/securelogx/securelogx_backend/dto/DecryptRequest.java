package com.securelogx.securelogx_backend.dto;

public class DecryptRequest {

    private String encryptedPath;
    private String password;

    public DecryptRequest() {}

    public String getEncryptedPath() {
        // 🔧 Normalize path to avoid Windows/Linux issues
        if (encryptedPath == null) {
            return null;
        }
        return encryptedPath.trim().replace("\\", "/");
    }

    public void setEncryptedPath(String encryptedPath) {
        this.encryptedPath = encryptedPath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

