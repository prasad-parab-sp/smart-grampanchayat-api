package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Maps {@code public.certificate_type_field} on the district shard (PostgreSQL). */
@Entity
@Table(name = "certificate_type_field")
public class CertificateTypeField {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "certificate_type_id", nullable = false)
    private UUID certificateTypeId;

    @Column(name = "field_key", nullable = false, length = 120)
    private String fieldKey;

    @Column(name = "label_mr", nullable = false, length = 500)
    private String labelMr;

    @Column(name = "placeholder_mr", length = 500)
    private String placeholderMr;

    @Column(name = "help_text_mr")
    private String helpTextMr;

    @Column(name = "data_type", nullable = false, length = 32)
    private String dataType;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json")
    private JsonNode optionsJson;

    @Column(name = "max_files")
    private Short maxFiles;

    @Column(name = "max_bytes")
    private Long maxBytes;

    @Column(name = "label_en", length = 500)
    private String labelEn;

    @Column(name = "placeholder_en", length = 500)
    private String placeholderEn;

    @Column(name = "help_text_en")
    private String helpTextEn;

    public CertificateTypeField() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCertificateTypeId() {
        return certificateTypeId;
    }

    public void setCertificateTypeId(UUID certificateTypeId) {
        this.certificateTypeId = certificateTypeId;
    }

    public String getFieldKey() {
        return fieldKey;
    }

    public void setFieldKey(String fieldKey) {
        this.fieldKey = fieldKey;
    }

    public String getLabelMr() {
        return labelMr;
    }

    public void setLabelMr(String labelMr) {
        this.labelMr = labelMr;
    }

    public String getPlaceholderMr() {
        return placeholderMr;
    }

    public void setPlaceholderMr(String placeholderMr) {
        this.placeholderMr = placeholderMr;
    }

    public String getHelpTextMr() {
        return helpTextMr;
    }

    public void setHelpTextMr(String helpTextMr) {
        this.helpTextMr = helpTextMr;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public JsonNode getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(JsonNode optionsJson) {
        this.optionsJson = optionsJson;
    }

    public Short getMaxFiles() {
        return maxFiles;
    }

    public void setMaxFiles(Short maxFiles) {
        this.maxFiles = maxFiles;
    }

    public Long getMaxBytes() {
        return maxBytes;
    }

    public void setMaxBytes(Long maxBytes) {
        this.maxBytes = maxBytes;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public String getPlaceholderEn() {
        return placeholderEn;
    }

    public void setPlaceholderEn(String placeholderEn) {
        this.placeholderEn = placeholderEn;
    }

    public String getHelpTextEn() {
        return helpTextEn;
    }

    public void setHelpTextEn(String helpTextEn) {
        this.helpTextEn = helpTextEn;
    }
}
