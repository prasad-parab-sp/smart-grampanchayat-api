package com.asset.smartgrampanchayatapi.master.jpa.crypto;

import org.springframework.stereotype.Component;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Hibernate maps plaintext {@link com.asset.smartgrampanchayatapi.master.jpa.model.District}{@code #dbPassword} ↔ persisted column.
 */
@Converter(autoApply = false)
@Component
public class DistrictDbPasswordAttributeConverter implements AttributeConverter<String, String> {

    private final DistrictPasswordCrypto crypto;

    public DistrictDbPasswordAttributeConverter(DistrictPasswordCrypto crypto) {
        this.crypto = crypto;
    }

    @Override
    public String convertToDatabaseColumn(String plaintextOnEntity) {
        if (plaintextOnEntity == null) {
            return null;
        }
        try {
            return crypto.encrypt(plaintextOnEntity);
        } catch (Exception e) {
            throw new IllegalStateException("Could not encrypt district db_password for persistence", e);
        }
    }

    /** Column value read from JDBC → decrypted/plaintext assigned to entity. */
    @Override
    public String convertToEntityAttribute(String columnValueStored) {
        return crypto.decrypt(columnValueStored);
    }
}
