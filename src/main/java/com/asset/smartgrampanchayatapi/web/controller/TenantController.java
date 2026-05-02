package com.asset.smartgrampanchayatapi.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.service.tenant.ShardTenantService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "District shard tenant APIs")
public class TenantController {

    private final ShardTenantService shardTenantService;

    public TenantController(ShardTenantService shardTenantService) {
        this.shardTenantService = shardTenantService;
    }

    @GetMapping
    @Operation(summary = "Get district tenant by tenant code (master routing, then shard tenants table)")
    @ApiResponse(
            responseCode = "200",
            description = "Tenant found"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content
    )
    @ApiResponse(
            responseCode = "503",
            description = "District database unavailable",
            content = @Content
    )
    public ResponseEntity<ShardTenant> getTenant(
            @RequestParam("tenantCode") String tenantCode
    ) {
        if (tenantCode == null || tenantCode.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return shardTenantService
                .findByTenantCode(tenantCode.trim())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
