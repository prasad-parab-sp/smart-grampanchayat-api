package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Catalog row from district shard {@code certificate_type} (platform {@code tenant_id} null, or tenant-specific).
 */
@Entity
@Table(name = "certificate_type")
public class CertificateType {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id")
    private UUID tenantId;

    @Column(nullable = false, length = 64)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CertificateTypeCategory category;

    @Column(name = "name_mr", nullable = false, length = 300)
    private String nameMr;

    @Column(name = "name_en", length = 300)
    private String nameEn;

    @Column(name = "description_mr", columnDefinition = "text")
    private String descriptionMr;

    @Column(name = "description_en", columnDefinition = "text")
    private String descriptionEn;

    @Column(name = "extra_fields_section_title_mr", length = 300)
    private String extraFieldsSectionTitleMr;

    @Column(name = "extra_fields_section_title_en", length = 300)
    private String extraFieldsSectionTitleEn;

    @Column(name = "default_fee_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultFeeAmount;

    @Column(name = "estimated_days_txt", length = 80)
    private String estimatedDaysTxt;

    /**
     * Optional emoji or short glyph for catalog UI; null means client may use category default.
     */
    @Column(length = 32)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected CertificateType() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CertificateTypeCategory getCategory() {
        return category;
    }

    public void setCategory(CertificateTypeCategory category) {
        this.category = category;
    }

    public String getNameMr() {
        return nameMr;
    }

    public void setNameMr(String nameMr) {
        this.nameMr = nameMr;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getDescriptionMr() {
        return descriptionMr;
    }

    public void setDescriptionMr(String descriptionMr) {
        this.descriptionMr = descriptionMr;
    }

    public String getDescriptionEn() {
        return descriptionEn;
    }

    public void setDescriptionEn(String descriptionEn) {
        this.descriptionEn = descriptionEn;
    }

    public String getExtraFieldsSectionTitleMr() {
        return extraFieldsSectionTitleMr;
    }

    public void setExtraFieldsSectionTitleMr(String extraFieldsSectionTitleMr) {
        this.extraFieldsSectionTitleMr = extraFieldsSectionTitleMr;
    }

    public String getExtraFieldsSectionTitleEn() {
        return extraFieldsSectionTitleEn;
    }

    public void setExtraFieldsSectionTitleEn(String extraFieldsSectionTitleEn) {
        this.extraFieldsSectionTitleEn = extraFieldsSectionTitleEn;
    }

    public BigDecimal getDefaultFeeAmount() {
        return defaultFeeAmount;
    }

    public void setDefaultFeeAmount(BigDecimal defaultFeeAmount) {
        this.defaultFeeAmount = defaultFeeAmount;
    }

    public String getEstimatedDaysTxt() {
        return estimatedDaysTxt;
    }

    public void setEstimatedDaysTxt(String estimatedDaysTxt) {
        this.estimatedDaysTxt = estimatedDaysTxt;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
