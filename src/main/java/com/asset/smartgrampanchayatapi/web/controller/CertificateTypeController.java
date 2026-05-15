package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.service.certificate.CertificateTypeService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeUpsertRequest;
import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificate-types")
@Tag(name = "Certificate types", description = "District shard certificate types for the tenant (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class CertificateTypeController {

    private final CertificateTypeService certificateTypeService;

    public CertificateTypeController(CertificateTypeService certificateTypeService) {
        this.certificateTypeService = certificateTypeService;
    }

    @GetMapping
    @Operation(
            summary = "Find visible certificate types for the tenant",
            description = "Returns platform certificate types (tenant_id null) plus any tenant-specific types, "
                    + "respecting tenant_certificate_type_config when platform types are disabled. "
                    + "Each item includes defaultFeeAmount (platform), feeAmount (resolved server-side for the tenant), and optional nested tenantCertificateTypeConfig when a tenant_certificate_type_config row exists. "
                    + "Optional query param `category` filters to CERTIFICATE, REGISTRATION, PERMISSIONS, or OTHERS."
    )
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @Parameter(
            name = "category",
            in = ParameterIn.QUERY,
            required = false,
            description = "Catalog group filter; omit to return all categories"
    )
    @ApiResponse(
            responseCode = "200",
            description = "List (may be empty)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificateTypeDto.class)))
    )
    @ApiResponse(responseCode = "404", description = "Unknown tenant code", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<List<CertificateTypeDto>> list(
            @RequestParam(value = "category", required = false) CertificateTypeCategory category
    ) {
        return certificateTypeService
                .findVisibleCertificateTypesForTenant(category)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code or tenant could not be resolved."
                ));
    }

    @GetMapping("/tenant-owned")
    @Operation(
            summary = "List tenant-owned certificate types (admin)",
            description = "Returns only rows where tenant_id matches X-Tenant-Code, including inactive types. "
                    + "For GP Admin / System Admin catalog management."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificateTypeDto.class)))
    )
    @ApiResponse(responseCode = "404", description = "Unknown tenant code", content = @Content)
    public ResponseEntity<List<CertificateTypeDto>> listTenantOwned() {
        return ResponseEntity.ok(certificateTypeService.listTenantOwnedCertificateTypes());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get one tenant-owned certificate type by id (admin)",
            description = "Loads a tenant-scoped row for edit, including inactive types. Platform types are not returned."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CertificateTypeDto.class)))
    @ApiResponse(responseCode = "404", description = "Tenant certificate type not found", content = @Content)
    public ResponseEntity<CertificateTypeDto> getTenantOwnedById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(certificateTypeService.getTenantOwnedCertificateTypeById(id));
    }

    @PostMapping
    @Operation(
            summary = "Create a tenant-owned certificate type",
            description = "Inserts into district shard table certificate_type with tenant_id set from X-Tenant-Code. "
                    + "Requires verified staff credentials; only GP_ADMIN or SYS_ADMIN (effective role) may call. "
                    + "Code must be unique for the tenant and must not match a platform type (case-insensitive). "
                    + "Optional extraFields become rows in certificate_type_field."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = CertificateTypeDto.class)))
    @ApiResponse(responseCode = "401", description = "Invalid staff credentials", content = @Content)
    @ApiResponse(responseCode = "403", description = "Caller is not GP Admin or System Admin", content = @Content)
    @ApiResponse(responseCode = "404", description = "Unknown tenant code", content = @Content)
    @ApiResponse(responseCode = "409", description = "Code conflict with platform or duplicate tenant code", content = @Content)
    public ResponseEntity<CertificateTypeDto> create(@Valid @RequestBody CertificateTypeCreateRequest body) {
        CertificateTypeDto saved = certificateTypeService.createCertificateType(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update a tenant-owned certificate type",
            description = "Updates mutable fields and replaces extraFields. Request body matches "
                    + "CertificateTypeUpsertRequest (same shape as create); catalog code is ignored / immutable. "
                    + "Secured at the edge like other district APIs until staff JWT exists."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CertificateTypeDto.class)))
    @ApiResponse(responseCode = "404", description = "Tenant certificate type not found", content = @Content)
    public ResponseEntity<CertificateTypeDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CertificateTypeUpsertRequest body
    ) {
        return ResponseEntity.ok(certificateTypeService.updateCertificateType(id, body));
    }
}
