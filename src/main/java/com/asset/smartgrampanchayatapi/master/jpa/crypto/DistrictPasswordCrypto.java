package com.asset.smartgrampanchayatapi.master.jpa.crypto;

import java.security.GeneralSecurityException;

import org.springframework.stereotype.Component;

import com.asset.smartgrampanchayatapi.master.config.DistrictCredentialEncryptionProperties;

/**
 * Stateless helper used by {@link DistrictDbPasswordAttributeConverter}: envelope strings in master DB ↔ plaintext entity field.
 */
@Component
public class DistrictPasswordCrypto {

    private final DistrictPasswordEnvelopeCodec codec;

    public DistrictPasswordCrypto(DistrictCredentialEncryptionProperties properties) {
        try {
            this.codec = new DistrictPasswordEnvelopeCodec(properties);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Invalid app.district-shard.credentials.secret-key-base64", e);
        }
    }

    /** Exposed so ops can migrate rows / scripts reusing envelope format. */
    public boolean envelopePrefixMatches(String candidate) {
        return codec.isEnvelope(candidate);
    }

    /** Column value from DB → plaintext for the entity field. */
    public String decrypt(String envelopeOrNull) {
        return codec.decrypt(envelopeOrNull);
    }

    /** Plaintext from entity field → value stored in {@code districts.db_password}. */
    public String encrypt(String plaintextOrNull) throws GeneralSecurityException {
        return codec.encrypt(plaintextOrNull);
    }
}
