package com.asset.smartgrampanchayatapi.web.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.service.tax.TaxTypeService;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypePatchRequest;
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
@RequestMapping("/api/tax-types")
@Tag(name = "Tax types", description = "Per-tenant tax catalog (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class TaxTypeController {

    private final TaxTypeService taxTypeService;

    public TaxTypeController(TaxTypeService taxTypeService) {
        this.taxTypeService = taxTypeService;
    }

    @GetMapping
    @Operation(summary = "List tax types for the tenant")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaxTypeDto.class)))
    )
    public ResponseEntity<List<TaxTypeDto>> list(
            @RequestParam(value = "activeOnly", defaultValue = "true") boolean activeOnly
    ) {
        return taxTypeService
                .listTaxTypes(activeOnly)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tenant code."));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one tax type by id")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<TaxTypeDto> getById(@PathVariable("id") UUID id) {
        return taxTypeService
                .getTaxType(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tax type not found."));
    }

    @PostMapping
    @Operation(summary = "Create a tax type (gramsevak / operator / GP admin)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = TaxTypeDto.class)))
    public ResponseEntity<TaxTypeDto> create(@Valid @RequestBody TaxTypeCreateRequest body) {
        TaxTypeDto saved = taxTypeService.createTaxType(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a tax type (names, description, active flag)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<TaxTypeDto> patch(@PathVariable("id") UUID id, @Valid @RequestBody TaxTypePatchRequest body) {
        return ResponseEntity.ok(taxTypeService.updateTaxType(id, body));
    }
}
