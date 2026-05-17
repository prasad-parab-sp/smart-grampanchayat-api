package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.util.Locale;

/** Mirrors PostgreSQL enum {@code tenant_status}. */
public enum TenantStatus {
    active,
    inactive,
    suspended,
    trial;

    public static TenantStatus fromApiValue(String raw) {
        return valueOf(raw.trim().toLowerCase(Locale.ROOT));
    }
}
