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

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.jpa.model.GenderType;
import com.asset.smartgrampanchayatapi.district.service.citizen.CitizenRegisterFilter;
import com.asset.smartgrampanchayatapi.district.service.citizen.CitizenService;
import com.asset.smartgrampanchayatapi.web.dto.CitizenCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CitizenDto;
import com.asset.smartgrampanchayatapi.web.dto.CitizenRegisterResponse;
import com.asset.smartgrampanchayatapi.web.dto.CitizenUpdateRequest;
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
@RequestMapping("/api/citizens")
@Tag(name = "Citizens", description = "District shard citizen APIs (requires " + TenantCodeHeaderFilter.HEADER_TENANT_CODE + ")")
public class CitizenController {

    private final CitizenService citizenService;

    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @GetMapping("/register")
    @Operation(
            summary = "List citizens for the villager register",
            description = "Returns filtered citizens and summary stats for gramsevak / sarpanch UI."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CitizenRegisterResponse> listRegister(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "gender", required = false) GenderType gender,
            @RequestParam(value = "filter", required = false, defaultValue = "active") CitizenRegisterFilter filter
    ) {
        return citizenService
                .listRegister(search, gender, filter)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    @PostMapping
    @Operation(summary = "Create a citizen register entry (gramsevak or sarpanch)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(responseCode = "201")
    public ResponseEntity<CitizenDto> create(@Valid @RequestBody CitizenCreateRequest body) {
        CitizenDto saved = citizenService.createCitizen(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a citizen register entry (gramsevak or sarpanch)")
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<CitizenDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody CitizenUpdateRequest body
    ) {
        return ResponseEntity.ok(citizenService.updateCitizen(id, body));
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Citizen not found for this tenant."
                ));
    }

    @GetMapping
    @Operation(
            summary = "List citizens for the tenant",
            description = "Collection GET — returns all non-deleted citizens (default filter=all). "
                    + "Optional search/gender/filter query params. For login lookup by contact, use GET /api/citizens/lookup."
    )
    @Parameter(name = TenantCodeHeaderFilter.HEADER_TENANT_CODE, in = ParameterIn.HEADER, required = true)
    @ApiResponse(
            responseCode = "200",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CitizenDto.class)))
    )
    public ResponseEntity<List<CitizenDto>> list(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "gender", required = false) GenderType gender,
            @RequestParam(value = "filter", required = false, defaultValue = "all") CitizenRegisterFilter filter
    ) {
        return citizenService
                .listRegister(search, gender, filter)
                .map(response -> ResponseEntity.ok(response.citizens()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Get citizen by mobile or email",
            description = "Exactly one of `mobile` or `email` (e.g. citizen login)."
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
    public ResponseEntity<Citizen> lookupByContact(
            @RequestParam(value = "mobile", required = false) String mobile,
            @RequestParam(value = "email", required = false) String email
    ) {
        boolean hasMobile = mobile != null && !mobile.isBlank();
        boolean hasEmail = email != null && !email.isBlank();
        if (hasMobile == hasEmail) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide exactly one of query parameters 'mobile' or 'email'."
            );
        }
        return citizenService
                .findByMobileOrEmail(mobile, email)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen not found."));
    }
}
