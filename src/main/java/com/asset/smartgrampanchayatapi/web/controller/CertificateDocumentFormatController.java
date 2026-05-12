package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.service.certificate.CertificateDocumentFormatService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateDocumentFormatDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateDocumentFormatUpsertRequest;
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
@RequestMapping("/api/certificate-document-formats")
@Tag(
        name = "Certificate document formats",
        description = "Tenant HTML templates for certificates (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")"
)
public class CertificateDocumentFormatController {

    private final CertificateDocumentFormatService certificateDocumentFormatService;

    public CertificateDocumentFormatController(CertificateDocumentFormatService certificateDocumentFormatService) {
        this.certificateDocumentFormatService = certificateDocumentFormatService;
    }

    @GetMapping
    @Operation(summary = "List all certificate document formats for the tenant")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CertificateDocumentFormatDto.class)))
    )
    public ResponseEntity<List<CertificateDocumentFormatDto>> list() {
        return ResponseEntity.ok(certificateDocumentFormatService.list());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one format by id")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CertificateDocumentFormatDto> getById(@PathVariable("id") UUID id) {
        return certificateDocumentFormatService
                .getById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Format not found."));
    }

    @PostMapping
    @Operation(summary = "Create a certificate document format")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CertificateDocumentFormatDto> create(@Valid @RequestBody CertificateDocumentFormatUpsertRequest body) {
        CertificateDocumentFormatDto saved = certificateDocumentFormatService.create(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a certificate document format")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CertificateDocumentFormatDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CertificateDocumentFormatUpsertRequest body
    ) {
        return ResponseEntity.ok(certificateDocumentFormatService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a certificate document format")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        certificateDocumentFormatService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
