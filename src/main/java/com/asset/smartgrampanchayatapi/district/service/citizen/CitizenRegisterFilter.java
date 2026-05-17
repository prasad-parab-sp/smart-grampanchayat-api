package com.asset.smartgrampanchayatapi.district.service.citizen;

/** Query filter for the villager / citizen register list (mirrors legacy gramjan UI). */
public enum CitizenRegisterFilter {
    active,
    all,
    voter,
    bpl,
    disabled,
    deceased,
    migrated
}
