package com.asset.smartgrampanchayatapi.master.jpa.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.asset.smartgrampanchayatapi.master.config.DistrictCredentialEncryptionProperties;

/**
 * AES-256/GCM envelopes for {@code districts.db_password}. Plaintext values in the DB are not supported.
 */
public final class DistrictPasswordEnvelopeCodec {

    public static final String ENVELOPE_PREFIX_V1 = "gpenc:v1:";

    private static final String CIPHER_ALG = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BYTES = 16;
    private static final int GCM_IV_BYTES = 12;
    private final SecretKey aesKey256;

    DistrictPasswordEnvelopeCodec(DistrictCredentialEncryptionProperties properties) throws GeneralSecurityException {
        String b64 = properties.getSecretKeyBase64();
        if (b64 == null || b64.isEmpty()) {
            throw new IllegalStateException("app.district-shard.credentials.secret-key-base64 must be set");
        }
        byte[] raw = Base64.getDecoder().decode(b64);
        if (raw.length != 32) {
            throw new GeneralSecurityException("app.district-shard.credentials.secret-key-base64 must decode to exactly 32 bytes (AES-256)");
        }
        this.aesKey256 = new SecretKeySpec(raw, "AES");
    }

    boolean isEnvelope(String stored) {
        return stored != null && stored.startsWith(ENVELOPE_PREFIX_V1);
    }

    /** Stored column value ({@link #ENVELOPE_PREFIX_V1}…) → plaintext. Rejects non-envelope values. */
    String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        if (!isEnvelope(stored)) {
            throw new IllegalStateException(
                    "districts.db_password must be an encrypted value starting with "
                            + ENVELOPE_PREFIX_V1
                            + "; plaintext passwords in the database are not supported"
            );
        }
        try {
            String base64Cipher = stored.substring(ENVELOPE_PREFIX_V1.length());
            byte[] decoded = Base64.getDecoder().decode(base64Cipher);
            if (decoded.length < GCM_IV_BYTES + GCM_TAG_BYTES + 1) {
                throw new GeneralSecurityException("Corrupt ciphertext (too short)");
            }
            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_BYTES);
            byte[] ct = Arrays.copyOfRange(decoded, GCM_IV_BYTES, decoded.length);

            Cipher cipher = Cipher.getInstance(CIPHER_ALG);
            cipher.init(Cipher.DECRYPT_MODE, aesKey256, new GCMParameterSpec(GCM_TAG_BYTES * 8, iv));
            byte[] plainBytes = cipher.doFinal(ct);
            return new String(plainBytes, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to decrypt district DB password envelope", e);
        }
    }

    /** Plaintext → stored column value (prefix + Base64 blob). */
    String encrypt(String plaintext) throws GeneralSecurityException {
        if (plaintext == null) {
            return null;
        }
        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);
        Cipher cipher = Cipher.getInstance(CIPHER_ALG);
        byte[] iv = new byte[GCM_IV_BYTES];
        SecureRandom rnd = SecureRandomHolder.INSTANCE;
        rnd.nextBytes(iv);

        cipher.init(Cipher.ENCRYPT_MODE, aesKey256, new GCMParameterSpec(GCM_TAG_BYTES * 8, iv));
        byte[] cipherPlusTag = cipher.doFinal(plaintextBytes);

        ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherPlusTag.length);
        buffer.put(iv);
        buffer.put(cipherPlusTag);
        return ENVELOPE_PREFIX_V1 + Base64.getEncoder().encodeToString(buffer.array());
    }

    private static final class SecureRandomHolder {
        private static final SecureRandom INSTANCE = new SecureRandom();
    }
}
