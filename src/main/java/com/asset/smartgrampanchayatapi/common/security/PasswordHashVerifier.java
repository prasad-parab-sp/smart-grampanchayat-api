package com.asset.smartgrampanchayatapi.common.security;

import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Shared password check for district {@code users} and master {@code super_admins} (BCrypt or legacy plain text in dev).
 */
public final class PasswordHashVerifier {

    private PasswordHashVerifier() {
    }

    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        String raw = rawPassword == null ? "" : rawPassword;
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return BCrypt.checkpw(raw, storedHash);
        }
        return storedHash.equals(raw);
    }
}
