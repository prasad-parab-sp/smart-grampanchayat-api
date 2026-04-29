package com.asset.smartgrampanchayatapi.master.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * AES-256 key for {@code districts.db_password} envelopes (required). Never store plaintext passwords.
 */
@ConfigurationProperties(prefix = "app.district-shard.credentials")
@Validated
public class DistrictCredentialEncryptionProperties {

    /**
     * Raw 256-bit AES key, Base64. Example: {@code openssl rand -base64 32}.
     * Production: secrets manager / env only.
     */
    @NotBlank(message = "app.district-shard.credentials.secret-key-base64 must be set (Base64-encoded 32-byte AES key)")
    private String secretKeyBase64;

    public String getSecretKeyBase64() {
        return secretKeyBase64;
    }

    public void setSecretKeyBase64(String secretKeyBase64) {
        this.secretKeyBase64 = secretKeyBase64 != null ? secretKeyBase64.trim() : "";
    }
}
