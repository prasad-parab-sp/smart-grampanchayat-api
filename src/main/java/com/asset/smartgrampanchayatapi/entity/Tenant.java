package com.asset.smartgrampanchayatapi.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * Human-readable id (e.g. GP-MH-…). Kept optional so Hibernate can add the column
     * to an existing table that already has rows; backfill in SQL, then enforce NOT NULL in DB.
     */
    @Column(name = "tenant_id", unique = true, length = 20)
    private String tenantId;

    @Column(name = "tenant_code", nullable = false, unique = true, length = 10)
    private String tenantCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "gp_code", nullable = false, length = 20)
    private String gpCode;

    @Column(name = "display_name_mr", length = 255)
    private String displayNameMr;

    @Column(name = "display_name_en", length = 255)
    private String displayNameEn;

    @Column(name = "taluka_mr", length = 255)
    private String talukaMr;

    @Column(name = "taluka_en", length = 255)
    private String talukaEn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;


    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "plan_type", nullable = false, length = 32)
    private String planType;

    @Column(name = "subscription_start_date")
    private LocalDate subscriptionStartDate;

    @Column(name = "subscription_end_date")
    private LocalDate subscriptionEndDate;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "contact_mobile", length = 15)
    private String contactMobile;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Tenant() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantCode() {
        return tenantCode;
    }

    public void setTenantCode(String tenantCode) {
        this.tenantCode = tenantCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGpCode() {
        return gpCode;
    }

    public void setGpCode(String gpCode) {
        this.gpCode = gpCode;
    }

    public String getDisplayNameMr() {
        return displayNameMr;
    }

    public void setDisplayNameMr(String displayNameMr) {
        this.displayNameMr = displayNameMr;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public void setDisplayNameEn(String displayNameEn) {
        this.displayNameEn = displayNameEn;
    }

    public String getTalukaMr() {
        return talukaMr;
    }

    public void setTalukaMr(String talukaMr) {
        this.talukaMr = talukaMr;
    }

    public String getTalukaEn() {
        return talukaEn;
    }

    public void setTalukaEn(String talukaEn) {
        this.talukaEn = talukaEn;
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        this.district = district;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public LocalDate getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public void setSubscriptionStartDate(LocalDate subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }

    public LocalDate getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public void setSubscriptionEndDate(LocalDate subscriptionEndDate) {
        this.subscriptionEndDate = subscriptionEndDate;
    }

    public Integer getMaxUsers() {
        return maxUsers;
    }

    public void setMaxUsers(Integer maxUsers) {
        this.maxUsers = maxUsers;
    }

    public String getContactMobile() {
        return contactMobile;
    }

    public void setContactMobile(String contactMobile) {
        this.contactMobile = contactMobile;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
