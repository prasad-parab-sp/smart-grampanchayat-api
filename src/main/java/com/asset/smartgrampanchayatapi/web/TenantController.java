package com.asset.smartgrampanchayatapi.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.service.DistrictTenantLookupService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "Tenant lookup APIs")
public class TenantController {

    private final DistrictTenantLookupService districtTenantLookupService;

    public TenantController(DistrictTenantLookupService districtTenantLookupService) {
        this.districtTenantLookupService = districtTenantLookupService;
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
        return districtTenantLookupService
                .findByTenantCode(tenantCode.trim())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
