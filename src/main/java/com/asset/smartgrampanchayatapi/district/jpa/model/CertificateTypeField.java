package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Extra form field definition for {@link CertificateType} (district shard {@code certificate_type_field}).
 * Values submitted with an application live in {@code certificate_application.additional_values_json} keyed by
 * {@link #fieldKey} (except FILE types, stored in {@code certificate_application_file}).
 */
@Entity
@Table(name = "certificate_type_field")
public class CertificateTypeField {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "certificate_type_id", nullable = false)
    private UUID certificateTypeId;

    @Column(name = "field_key", nullable = false, length = 120)
    private String fieldKey;

    @Column(name = "label_mr", nullable = false, length = 500)
    private String labelMr;

    @Column(name = "label_en", length = 500)
    private String labelEn;

    @Column(name = "placeholder_mr", length = 500)
    private String placeholderMr;

    @Column(name = "placeholder_en", length = 500)
    private String placeholderEn;

    @Column(name = "help_text_mr", columnDefinition = "text")
    private String helpTextMr;

    @Column(name = "help_text_en", columnDefinition = "text")
    private String helpTextEn;

    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType;

    /** Maps to {@code certificate_type_field.required}. */
    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "jsonb")
    private JsonNode optionsJson;

    @Column(name = "max_files")
    private Short maxFiles;

    @Column(name = "max_bytes")
    private Long maxBytes;

    @Column(name = "allowed_mime_csv", length = 300)
    private String allowedMimeCsv;

    protected CertificateTypeField() {
    }

    public UUID getId() {
        return id;
    }

    public UUID getCertificateTypeId() {
        return certificateTypeId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public String getLabelMr() {
        return labelMr;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public String getPlaceholderMr() {
        return placeholderMr;
    }

    public String getPlaceholderEn() {
        return placeholderEn;
    }

    public String getHelpTextMr() {
        return helpTextMr;
    }

    public String getHelpTextEn() {
        return helpTextEn;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public JsonNode getOptionsJson() {
        return optionsJson;
    }

    public Short getMaxFiles() {
        return maxFiles;
    }

    public Long getMaxBytes() {
        return maxBytes;
    }

    public String getAllowedMimeCsv() {
        return allowedMimeCsv;
    }
}
