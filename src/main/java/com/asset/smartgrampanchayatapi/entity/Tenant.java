package com.asset.smartgrampanchayatapi.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * District {@code tenants} row: header / GP metadata (denormalized district & taluka). No {@code district_id} FK in this
 * shape — names come from {@code district_name_*} / {@code taluka_*}. In a single dev DB with central {@code tenants} also
 * present, this entity maps to {@code district_tenants}; use {@code tenants} when this PU only sees the shard.
 */
@Entity
@Table(name = "district_tenants")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Tenant {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 32)
    private String tenantId;

    @Column(name = "tenant_code", nullable = false, unique = true, length = 10)
    private String tenantCode;

    @Column(nullable = false)
    private String name;

    @Column(name = "display_name_en")
    private String displayNameEn;

    @Column(name = "display_name_mr")
    private String displayNameMr;

    @Column(name = "gp_code", nullable = false, length = 50)
    private String gpCode;

    @Column(name = "district_name_en")
    private String districtNameEn;

    @Column(name = "district_name_mr")
    private String districtNameMr;

    @Column(name = "taluka_en")
    private String talukaEn;

    @Column(name = "taluka_mr")
    private String talukaMr;

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

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

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

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public void setDisplayNameEn(String displayNameEn) {
        this.displayNameEn = displayNameEn;
    }

    public String getDisplayNameMr() {
        return displayNameMr;
    }

    public void setDisplayNameMr(String displayNameMr) {
        this.displayNameMr = displayNameMr;
    }

    public String getGpCode() {
        return gpCode;
    }

    public void setGpCode(String gpCode) {
        this.gpCode = gpCode;
    }

    public String getDistrictNameEn() {
        return districtNameEn;
    }

    public void setDistrictNameEn(String districtNameEn) {
        this.districtNameEn = districtNameEn;
    }

    public String getDistrictNameMr() {
        return districtNameMr;
    }

    public void setDistrictNameMr(String districtNameMr) {
        this.districtNameMr = districtNameMr;
    }

    public String getTalukaEn() {
        return talukaEn;
    }

    public void setTalukaEn(String talukaEn) {
        this.talukaEn = talukaEn;
    }

    public String getTalukaMr() {
        return talukaMr;
    }

    public void setTalukaMr(String talukaMr) {
        this.talukaMr = talukaMr;
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

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
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
