package com.asset.smartgrampanchayatapi.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserLoginResponse", description = "Successful login response")
public record UserLoginResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UserDto user,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Login successful")
        String message
) {
}
