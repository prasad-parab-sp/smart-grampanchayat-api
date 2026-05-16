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

import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTaxStatus;
import com.asset.smartgrampanchayatapi.district.service.tax.CitizenTaxService;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxBulkCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxBulkCreateResultDto;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxDto;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxWaiveRequest;
import com.asset.smartgrampanchayatapi.web.dto.TaxPaymentCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TaxPaymentDto;
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
@Tag(name = "Citizen taxes", description = "Tax demands and payments (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class CitizenTaxController {

    private final CitizenTaxService citizenTaxService;

    public CitizenTaxController(CitizenTaxService citizenTaxService) {
        this.citizenTaxService = citizenTaxService;
    }

    @GetMapping("/api/citizens/{citizenId}/taxes")
    @Operation(summary = "List tax demands for a citizen")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CitizenTaxDto.class)))
    )
    public ResponseEntity<List<CitizenTaxDto>> listForCitizen(@PathVariable("citizenId") UUID citizenId) {
        return citizenTaxService
                .listCitizenTaxes(citizenId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tenant code."));
    }

    @PostMapping("/api/citizens/{citizenId}/taxes")
    @Operation(summary = "Assign a tax demand to a citizen")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = CitizenTaxDto.class)))
    public ResponseEntity<CitizenTaxDto> createForCitizen(
            @PathVariable("citizenId") UUID citizenId,
            @Valid @RequestBody CitizenTaxCreateRequest body
    ) {
        CitizenTaxDto saved = citizenTaxService.createCitizenTax(citizenId, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/api/citizen-taxes/bulk")
    @Operation(summary = "Assign the same tax demand to one or more citizens")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = CitizenTaxBulkCreateResultDto.class)))
    public ResponseEntity<CitizenTaxBulkCreateResultDto> bulkCreate(@Valid @RequestBody CitizenTaxBulkCreateRequest body) {
        CitizenTaxBulkCreateResultDto result = citizenTaxService.bulkCreateCitizenTaxes(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/api/citizen-taxes")
    @Operation(summary = "List tax demands for the tenant (optional filters)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<List<CitizenTaxDto>> listForTenant(
            @RequestParam(value = "status", required = false) CitizenTaxStatus status,
            @RequestParam(value = "financialYear", required = false) String financialYear
    ) {
        return citizenTaxService
                .listTenantTaxes(status, financialYear)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown tenant code."));
    }

    @GetMapping("/api/citizen-taxes/{id}")
    @Operation(summary = "Get one citizen tax demand by id")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CitizenTaxDto> getById(@PathVariable("id") UUID id) {
        return citizenTaxService
                .getCitizenTax(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen tax not found."));
    }

    @GetMapping("/api/citizen-taxes/{id}/payments")
    @Operation(summary = "List payments for a citizen tax demand")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = TaxPaymentDto.class)))
    )
    public ResponseEntity<List<TaxPaymentDto>> listPayments(@PathVariable("id") UUID id) {
        return citizenTaxService
                .listPayments(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen tax not found."));
    }

    @PatchMapping("/api/citizen-taxes/{id}/waive")
    @Operation(summary = "Waive outstanding tax for a citizen demand")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CitizenTaxDto> waive(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CitizenTaxWaiveRequest body
    ) {
        return ResponseEntity.ok(citizenTaxService.waiveTax(id, body));
    }

    @PostMapping("/api/citizen-taxes/{id}/payments")
    @Operation(summary = "Record a payment against a citizen tax demand")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = TaxPaymentDto.class)))
    public ResponseEntity<TaxPaymentDto> recordPayment(
            @PathVariable("id") UUID id,
            @Valid @RequestBody TaxPaymentCreateRequest body
    ) {
        TaxPaymentDto saved = citizenTaxService.recordPayment(id, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
