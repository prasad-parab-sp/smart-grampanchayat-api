package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * District shard {@code users} row — login accounts for GP staff (not {@linkplain Citizen citizens}).
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uq_users_tenant_email", columnNames = {"tenant_id", "email"})
)
public class ShardUser {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "user_role")
    private UserRole role;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(length = 15)
    private String phone;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "password_changed_at")
    private Instant passwordChangedAt;

    /** When set with acting_from / acting_until in range, {@link #effectiveRoleAt} returns this instead of {@link #role}. */
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "elevated_role", columnDefinition = "user_role")
    private UserRole elevatedRole;

    @Column(name = "acting_from")
    private Instant actingFrom;

    @Column(name = "acting_until")
    private Instant actingUntil;

    protected ShardUser() {
    }

    /**
     * Role used for authorization / UI: {@link #elevatedRole} while {@code now} is in
     * {@code [acting_from, acting_until)}, otherwise {@link #role}.
     */
    public UserRole effectiveRoleAt(Instant now) {
        if (elevatedRole == null || actingFrom == null || actingUntil == null) {
            return role;
        }
        if (now.isBefore(actingFrom) || !now.isBefore(actingUntil)) {
            return role;
        }
        return elevatedRole;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getPasswordChangedAt() {
        return passwordChangedAt;
    }

    public void setPasswordChangedAt(Instant passwordChangedAt) {
        this.passwordChangedAt = passwordChangedAt;
    }

    public UserRole getElevatedRole() {
        return elevatedRole;
    }

    public void setElevatedRole(UserRole elevatedRole) {
        this.elevatedRole = elevatedRole;
    }

    public Instant getActingFrom() {
        return actingFrom;
    }

    public void setActingFrom(Instant actingFrom) {
        this.actingFrom = actingFrom;
    }

    public Instant getActingUntil() {
        return actingUntil;
    }

    public void setActingUntil(Instant actingUntil) {
        this.actingUntil = actingUntil;
    }
}
