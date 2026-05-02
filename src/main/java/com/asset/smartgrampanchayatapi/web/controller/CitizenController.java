package com.asset.smartgrampanchayatapi.web.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.service.citizen.CitizenService;
import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/citizens")
@Tag(name = "Citizens", description = "District shard citizen APIs (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get citizen by id",
            description = "Returns the citizen only if it belongs to the tenant from the "
                    + TenantCodeHeaderFilter.HEADER_TENANT_CODE + " header."
    )
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(responseCode = "200", description = "Citizen found")
    @ApiResponse(responseCode = "404", description = "Not found or not in this tenant", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<Citizen> getById(@PathVariable("id") UUID id) {
        return citizenService
                .findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
            summary = "Get citizen by mobile or email",
            description = "Exactly one of `mobile` or `email`. "
                    + "Shard is resolved using the " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + " header."
    )
    @Parameter(
            name = TenantCodeHeaderFilter.HEADER_TENANT_CODE,
            in = ParameterIn.HEADER,
            required = true,
            example = "GP001"
    )
    @ApiResponse(responseCode = "200", description = "Citizen found")
    @ApiResponse(
            responseCode = "400",
            description = "Missing or ambiguous query (need exactly one of mobile, email)",
            content = @Content
    )
    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
    @ApiResponse(responseCode = "503", description = "District database unavailable", content = @Content)
    public ResponseEntity<Citizen> getByContact(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "email", required = false) String email
    ) {
        boolean hasMobile = mobile != null && !mobile.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        if (hasMobile == hasEmail) {
            return ResponseEntity.badRequest().build();
        }
        return citizenService
                .findByMobileOrEmail(mobile, email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
