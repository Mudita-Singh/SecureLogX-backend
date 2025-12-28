package com.securelogx.securelogx_backend.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;

public final class UserStore {

    private static final String USER_FILE =
            System.getProperty("user.dir") + File.separator + "users.json";

    private static final ObjectMapper mapper = new ObjectMapper();

    private UserStore() {}

    /* =========================
       PUBLIC METHODS
       ========================= */

    public static boolean userExists(String username) {
        try {
            JsonNode users = readUsers();
            for (JsonNode user : users) {
                if (user.get("username").asText().equals(username)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean validate(String username, String password) {
        try {
            JsonNode users = readUsers();
            String hash = sha256(password);

            for (JsonNode user : users) {
                if (user.get("username").asText().equals(username) &&
                        user.get("passwordHash").asText().equals(hash)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void createUser(String username, String password) throws Exception {
        File file = new File(USER_FILE);
        ObjectNode root;
        ArrayNode users;

        if (file.exists()) {
            root = (ObjectNode) mapper.readTree(file);
            users = (ArrayNode) root.get("users");
        } else {
            root = mapper.createObjectNode();
            users = mapper.createArrayNode();
            root.set("users", users);
        }

        ObjectNode user = mapper.createObjectNode();
        user.put("username", username);
        user.put("passwordHash", sha256(password));

        users.add(user);
        Files.writeString(file.toPath(), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    }

    /* =========================
       INTERNAL HELPERS
       ========================= */

    private static JsonNode readUsers() throws Exception {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return mapper.createArrayNode();
        }
        JsonNode root = mapper.readTree(file);
        return root.get("users");
    }

    private static String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
