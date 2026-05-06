package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_application")
public class CertificateApplication {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "certificate_type_id", nullable = false)
    private UUID certificateTypeId;

    @Column(name = "application_number", nullable = false, length = 40)
    private String applicationNumber;

    @Column(name = "applicant_full_name", nullable = false, length = 300)
    private String applicantFullName;

    @Column(name = "applicant_mobile", nullable = false, length = 15)
    private String applicantMobile;

    @Column(name = "reason_short", length = 200)
    private String reasonShort;

    @Column(name = "reason_details", columnDefinition = "text")
    private String reasonDetails;

    @Column(name = "address_text", columnDefinition = "text")
    private String addressText;

    @Column(name = "for_whom_name", length = 300)
    private String forWhomName;

    @Column(name = "citizen_id", nullable = false)
    private UUID citizenId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private CertificateApplicationStatus status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payment_reference", length = 120)
    private String paymentReference;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "additional_values_json", nullable = false, columnDefinition = "jsonb")
    private JsonNode additionalValuesJson;

    public CertificateApplication() {
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

    public UUID getCertificateTypeId() {
        return certificateTypeId;
    }

    public void setCertificateTypeId(UUID certificateTypeId) {
        this.certificateTypeId = certificateTypeId;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getApplicantFullName() {
        return applicantFullName;
    }

    public void setApplicantFullName(String applicantFullName) {
        this.applicantFullName = applicantFullName;
    }

    public String getApplicantMobile() {
        return applicantMobile;
    }

    public void setApplicantMobile(String applicantMobile) {
        this.applicantMobile = applicantMobile;
    }

    public String getReasonShort() {
        return reasonShort;
    }

    public void setReasonShort(String reasonShort) {
        this.reasonShort = reasonShort;
    }

    public String getReasonDetails() {
        return reasonDetails;
    }

    public void setReasonDetails(String reasonDetails) {
        this.reasonDetails = reasonDetails;
    }

    public String getAddressText() {
        return addressText;
    }

    public void setAddressText(String addressText) {
        this.addressText = addressText;
    }

    public String getForWhomName() {
        return forWhomName;
    }

    public void setForWhomName(String forWhomName) {
        this.forWhomName = forWhomName;
    }

    public UUID getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(UUID citizenId) {
        this.citizenId = citizenId;
    }

    public CertificateApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(CertificateApplicationStatus status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public JsonNode getAdditionalValuesJson() {
        return additionalValuesJson;
    }

    public void setAdditionalValuesJson(JsonNode additionalValuesJson) {
        this.additionalValuesJson = additionalValuesJson;
    }
}
