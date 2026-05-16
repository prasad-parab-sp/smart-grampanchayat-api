package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tax_payment")
public class TaxPayment {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "citizen_tax_id", nullable = false)
    private UUID citizenTaxId;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_on", nullable = false)
    private LocalDate paidOn;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 16)
    private TaxPaymentMode paymentMode;

    @Column(name = "receipt_number", nullable = false, length = 32)
    private String receiptNumber;

    @Column(length = 100)
    private String reference;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public TaxPayment() {
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

    public UUID getCitizenTaxId() {
        return citizenTaxId;
    }

    public void setCitizenTaxId(UUID citizenTaxId) {
        this.citizenTaxId = citizenTaxId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(LocalDate paidOn) {
        this.paidOn = paidOn;
    }

    public TaxPaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(TaxPaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public UUID getRecordedByUserId() {
        return recordedByUserId;
    }

    public void setRecordedByUserId(UUID recordedByUserId) {
        this.recordedByUserId = recordedByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
