package com.asset.smartgrampanchayatapi.master.jpa.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "districts")
public class District {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "district_code", nullable = false, length = 20)
    private String districtCode;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "display_name_mr", length = 255)
    private String displayNameMr;

    @Column(name = "display_name_en", length = 255)
    private String displayNameEn;

    @Column(nullable = false, length = 100)
    private String state;

    @JsonIgnore
    @Column(name = "db_host", nullable = false, columnDefinition = "text")
    private String dbHost;

    @JsonIgnore
    @Column(name = "db_port", nullable = false)
    private Integer dbPort;

    @JsonIgnore
    @Column(name = "db_name", nullable = false, length = 100)
    private String dbName;

    @JsonIgnore
    @Column(name = "db_username", nullable = false, columnDefinition = "text")
    private String dbUsername;

    @JsonIgnore
    @Column(name = "db_password", nullable = false, columnDefinition = "text")
    private String dbPassword;

    @JsonIgnore
    @Column(name = "db_ssl_enabled", nullable = false)
    private Boolean dbSslEnabled;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "panchayat_count", nullable = false)
    private Integer panchayatCount;

    @Column(name = "district_admin_id")
    private UUID districtAdminId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected District() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = districtCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public Integer getDbPort() {
        return dbPort;
    }

    public void setDbPort(Integer dbPort) {
        this.dbPort = dbPort;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public Boolean getDbSslEnabled() {
        return dbSslEnabled;
    }

    public void setDbSslEnabled(Boolean dbSslEnabled) {
        this.dbSslEnabled = dbSslEnabled;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getPanchayatCount() {
        return panchayatCount;
    }

    public void setPanchayatCount(Integer panchayatCount) {
        this.panchayatCount = panchayatCount;
    }

    public UUID getDistrictAdminId() {
        return districtAdminId;
    }

    public void setDistrictAdminId(UUID districtAdminId) {
        this.districtAdminId = districtAdminId;
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
