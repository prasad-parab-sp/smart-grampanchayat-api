package com.asset.smartgrampanchayatapi.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.service.tenant.ShardTenantService;
import com.asset.smartgrampanchayatapi.district.service.tenant.TenantProvisioningService;
import com.asset.smartgrampanchayatapi.web.dto.TenantCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TenantProfileDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "District shard tenant APIs")
public class TenantController {

    private final ShardTenantService shardTenantService;
    private final TenantProvisioningService tenantProvisioningService;

    public TenantController(
            ShardTenantService shardTenantService,
            TenantProvisioningService tenantProvisioningService
    ) {
        this.shardTenantService = shardTenantService;
        this.tenantProvisioningService = tenantProvisioningService;
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
    public ResponseEntity<TenantProfileDto> getTenant(
            @RequestParam("tenantCode") String tenantCode
    ) {
        if (tenantCode == null || tenantCode.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query parameter 'tenantCode' is required.");
        }
        return shardTenantService
                .findProfileByTenantCode(tenantCode.trim())
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No tenant found for the given tenantCode."
                ));
    }

    @PostMapping
    @Operation(
            summary = "Provision a new gram panchayat tenant",
            description = "Inserts the routing row on the master database and the full tenant profile on the target district shard. "
                    + "Does not require X-Tenant-Code (tenant does not exist yet)."
    )
    @ApiResponse(responseCode = "201", description = "Tenant created")
    @ApiResponse(responseCode = "404", description = "District not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "tenantCode, tenantId, or gpCode already exists", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<TenantProfileDto> createTenant(@Valid @RequestBody TenantCreateRequest body) {
        TenantProfileDto created = tenantProvisioningService.createTenant(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
