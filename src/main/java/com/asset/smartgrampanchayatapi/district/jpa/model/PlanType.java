package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.util.Locale;

/** Mirrors PostgreSQL enum {@code plan_type}. */
public enum PlanType {
    basic,
    standard,
    premium;

    public static PlanType fromApiValue(String raw) {
        return valueOf(raw.trim().toLowerCase(Locale.ROOT));
    }
}
