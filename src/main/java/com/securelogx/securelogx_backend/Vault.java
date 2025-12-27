package com.securelogx.securelogx_backend;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Arrays;

public class Vault {

    private SecretKeySpec key(String password) throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest(password.getBytes());
        return new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
    }

    public String encryptFile(String path, String password) {
        try {
            File inputFile = new File(path);
            byte[] data = Files.readAllBytes(inputFile.toPath());

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key(password));

            String encPath = path + ".enc";
            Files.write(new File(encPath).toPath(), cipher.doFinal(data));

            System.out.println("✅ Encrypted file created at: " + encPath);
            return encPath;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Encryption failed: " + e.getMessage());
        }
    }

    public String decryptFile(String path, String password) {
        try {
            File encryptedFile = new File(path);

            System.out.println("🔐 Decrypting file: " + encryptedFile.getAbsolutePath());
            System.out.println("📁 Exists? " + encryptedFile.exists());

            byte[] data = Files.readAllBytes(encryptedFile.toPath());

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key(password));

            String outPath = path.replace(".enc", "_decrypted.json");
            Files.write(new File(outPath).toPath(), cipher.doFinal(data));

            System.out.println("✅ Decrypted file created at: " + outPath);
            return outPath;

        } catch (Exception e) {
            System.out.println("❌ DECRYPTION FAILED");
            e.printStackTrace();   // THIS IS WHAT WE NEED
            throw new RuntimeException("Decryption failed: " + e.getMessage());
        }
    }
}
