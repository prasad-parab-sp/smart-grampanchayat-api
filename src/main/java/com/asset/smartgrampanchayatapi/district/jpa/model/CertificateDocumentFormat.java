package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_document_format")
public class CertificateDocumentFormat {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "certificate_type_id")
    private UUID certificateTypeId;

    @Column(name = "display_name", nullable = false, length = 300)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "format_kind", nullable = false, length = 32)
    private DocumentFormatKind formatKind;

    @Column(name = "document_title", length = 500)
    private String documentTitle;

    @Column(name = "body_html", nullable = false, columnDefinition = "text")
    private String bodyHtml;

    @Column(name = "footer_note", columnDefinition = "text")
    private String footerNote;

    @Column(name = "internal_note", columnDefinition = "text")
    private String internalNote;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public CertificateDocumentFormat() {
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public DocumentFormatKind getFormatKind() {
        return formatKind;
    }

    public void setFormatKind(DocumentFormatKind formatKind) {
        this.formatKind = formatKind;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public String getFooterNote() {
        return footerNote;
    }

    public void setFooterNote(String footerNote) {
        this.footerNote = footerNote;
    }

    public String getInternalNote() {
        return internalNote;
    }

    public void setInternalNote(String internalNote) {
        this.internalNote = internalNote;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
