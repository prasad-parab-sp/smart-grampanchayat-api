package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "UserLoginRequest", description = "Login request using either email or mobile as identifier")
public record UserLoginRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "demo1_1778054664@gp.local")
        @NotBlank(message = "identifier is required")
        String identifier,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "password123")
        @NotBlank(message = "password is required")
        String password
) {
}
