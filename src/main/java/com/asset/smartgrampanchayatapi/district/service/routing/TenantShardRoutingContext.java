package com.asset.smartgrampanchayatapi.district.service.routing;

import java.util.UUID;

import com.asset.smartgrampanchayatapi.master.jpa.model.District;

/**
 * Result of resolving a {@code tenantCode} on the master DB: district DB to route to and shard tenant row id
 * (same uuid as {@link com.asset.smartgrampanchayatapi.master.jpa.model.MasterTenant#getId()}).
 */
public record TenantShardRoutingContext(District district, UUID tenantId, String tenantCode) {
}
