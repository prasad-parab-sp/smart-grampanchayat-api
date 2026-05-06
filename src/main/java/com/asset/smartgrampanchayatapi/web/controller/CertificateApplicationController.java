package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus;
import com.asset.smartgrampanchayatapi.district.service.certificate.CertificateApplicationService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateApplicationSubmitRequest;
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
@RequestMapping("/api/certificate-applications")
@Tag(
        name = "Certificate applications",
        description = "Submit and read certificate applications on the district shard (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")"
)
public class CertificateApplicationController {

    private final CertificateApplicationService certificateApplicationService;

    public CertificateApplicationController(CertificateApplicationService certificateApplicationService) {
        this.certificateApplicationService = certificateApplicationService;
    }

    @PostMapping
    @Operation(summary = "Submit a new certificate application")
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(schema = @Schema(implementation = CertificateApplicationDto.class))
    )
    @ApiResponse(responseCode = "400", description = "Validation error or certificate type not allowed", content = @Content)
    @ApiResponse(responseCode = "404", description = "Unknown tenant code", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<CertificateApplicationDto> submit(@Valid @RequestBody CertificateApplicationSubmitRequest body) {
        CertificateApplicationDto saved = certificateApplicationService.submit(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @Operation(
            summary = "List certificate applications for the tenant",
            description = "Optional filters: citizenId (UUID), status (enum). Results ordered by submittedAt descending."
    )
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @Parameter(
            name = "citizenId",
            in = ParameterIn.QUERY,
            description = "When set, only applications for this citizen (same tenant)"
    )
    @Parameter(
            name = "status",
            in = ParameterIn.QUERY,
            description = "When set, only applications in this status",
            schema = @Schema(implementation = CertificateApplicationStatus.class)
    )
    @ApiResponse(
            responseCode = "200",
            description = "List (may be empty)",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificateApplicationDto.class)))
    )
    @ApiResponse(responseCode = "404", description = "Unknown tenant code", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<List<CertificateApplicationDto>> list(
            @RequestParam(value = "citizenId", required = false) UUID citizenId,
            @RequestParam(value = "status", required = false) CertificateApplicationStatus status
    ) {
        return ResponseEntity.ok(certificateApplicationService.list(citizenId, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one certificate application by id (scoped to tenant)")
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(
            responseCode = "200",
            content = @Content(schema = @Schema(implementation = CertificateApplicationDto.class))
    )
    @ApiResponse(responseCode = "404", description = "Unknown tenant or application not found", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<CertificateApplicationDto> getById(@PathVariable("id") UUID id) {
        return certificateApplicationService
                .getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Certificate application not found for this tenant."
                ));
    }
}
