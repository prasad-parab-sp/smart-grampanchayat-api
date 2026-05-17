package com.asset.smartgrampanchayatapi.web.dto;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PlatformAdminLoginRequest", description = "Super-admin login with mobile and password")
public record PlatformAdminLoginRequest(
        @Schema(example = "9876543210")
        @NotBlank
        String mobile,
        @NotBlank String password
) {
}
