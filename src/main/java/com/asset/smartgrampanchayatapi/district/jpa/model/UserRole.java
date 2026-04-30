package com.asset.smartgrampanchayatapi.district.jpa.model;

/**
 * Mirrors PostgreSQL enum {@code user_role}.
 * Constant names must match database labels exactly.
 */
public enum UserRole {
    gp_admin,
    operator,
    viewer,
    sarpanch,
    gramsevak
}
