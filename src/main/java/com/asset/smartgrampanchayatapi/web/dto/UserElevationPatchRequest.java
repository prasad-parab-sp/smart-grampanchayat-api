package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;

import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * MVP: set all three fields to grant temporary powers, or send all nulls to clear.
 * Interval is {@code [actingFrom, actingUntil)} (end exclusive).
 */
@Schema(name = "UserElevationPatchRequest")
public record UserElevationPatchRequest(
        UserRole elevatedRole,
        Instant actingFrom,
        Instant actingUntil
) {
}
