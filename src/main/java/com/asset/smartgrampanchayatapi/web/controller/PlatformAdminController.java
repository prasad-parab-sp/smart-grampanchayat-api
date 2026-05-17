package com.asset.smartgrampanchayatapi.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asset.smartgrampanchayatapi.master.service.platform.PlatformAdminService;
import com.asset.smartgrampanchayatapi.web.dto.PlatformAdminLoginRequest;
import com.asset.smartgrampanchayatapi.web.dto.PlatformAdminLoginResponse;
import com.asset.smartgrampanchayatapi.web.dto.PlatformStatsDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/platform")
@Tag(name = "Platform admin", description = "Master-database super-admin login and platform statistics (no X-Tenant-Code)")
public class PlatformAdminController {

    private final PlatformAdminService platformAdminService;

    public PlatformAdminController(PlatformAdminService platformAdminService) {
        this.platformAdminService = platformAdminService;
    }

    @PostMapping("/auth/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Super-admin login", description = "Authenticates against master super_admins; updates last_login_at on success.")
    public PlatformAdminLoginResponse login(@Valid @RequestBody PlatformAdminLoginRequest body) {
        return platformAdminService.login(body.mobile(), body.password());
    }

    @GetMapping("/stats")
    @Operation(summary = "Platform overview counts", description = "District and gram panchayat (tenant) totals from the master database.")
    public PlatformStatsDto stats() {
        return platformAdminService.stats();
    }
}
