package com.asset.smartgrampanchayatapi.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asset.smartgrampanchayatapi.district.service.tenant.GrampanchayatOfficersService;
import com.asset.smartgrampanchayatapi.web.dto.GrampanchayatOfficersUpdateRequest;
import com.asset.smartgrampanchayatapi.web.filter.TenantCodeHeaderFilter;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/grampanchayat")
@Tag(name = "Gram panchayat", description = "Gram panchayat profile on district shard (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class GrampanchayatController {

    private final GrampanchayatOfficersService grampanchayatOfficersService;

    public GrampanchayatController(GrampanchayatOfficersService grampanchayatOfficersService) {
        this.grampanchayatOfficersService = grampanchayatOfficersService;
    }

    @PutMapping("/officers")
    @Operation(
            summary = "Update gramsevak display name on grampanchayat",
            description = "Updates {@code gramsevak_name}. Sarpanch is managed via {@code users} (role SARPANCH). "
                    + "Omit the field or send null to leave unchanged. Empty string clears."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Void> updateOfficers(@Valid @RequestBody GrampanchayatOfficersUpdateRequest body) {
        grampanchayatOfficersService.updateOfficers(body);
        return ResponseEntity.noContent().build();
    }
}
