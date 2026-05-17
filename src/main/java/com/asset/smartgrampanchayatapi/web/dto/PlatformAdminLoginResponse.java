package com.asset.smartgrampanchayatapi.web.dto;

public record PlatformAdminLoginResponse(
        String id,
        String mobile,
        String displayName,
        String role
) {
}
