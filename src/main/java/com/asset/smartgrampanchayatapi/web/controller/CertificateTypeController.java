package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.service.certificate.CertificateTypeService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeDto;
import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

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
}
