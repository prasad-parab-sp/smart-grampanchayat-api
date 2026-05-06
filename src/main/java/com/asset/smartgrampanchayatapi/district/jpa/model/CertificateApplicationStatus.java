package com.asset.smartgrampanchayatapi.district.jpa.model;

/**
 * Values must match the {@code certificate_application.status} check constraint in {@code certificate-module.sql}.
 */
public enum CertificateApplicationStatus {
    SUBMITTED,
    PENDING_PAYMENT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    CANCELLED
}
