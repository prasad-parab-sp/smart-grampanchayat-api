package com.asset.smartgrampanchayatapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asset.smartgrampanchayatapi.entity.Tenant;
import com.asset.smartgrampanchayatapi.repository.TenantRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "Tenant lookup APIs")
public class TenantController {

    private final TenantRepository tenantRepository;

    public TenantController(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @GetMapping
    @Operation(summary = "Get tenant by name, code, or display name (case-insensitive)")
    @ApiResponse(
            responseCode = "200",
            description = "Tenant found"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content
    )
    public ResponseEntity<Tenant> getTenant(
            @RequestParam("tenantName") String tenantName
    ) {
        if (tenantName == null || tenantName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return tenantRepository
                .findActiveByTenantNameOrCodeOrDisplayName(tenantName.trim())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
