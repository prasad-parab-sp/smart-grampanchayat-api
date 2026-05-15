package com.asset.smartgrampanchayatapi.district.jpa.model;

/**
 * Mirrors PostgreSQL enum {@code user_role}.
 * Constant names must match database labels exactly.
 */
public enum UserRole {
    SARPANCH,
    GRAMSEVAK,
    GP_ADMIN,
    /** Platform / district-wide administrator (may create tenant catalog entries such as certificate types). */
    SYS_ADMIN,
    OPERATOR,
    VIEWER
}
