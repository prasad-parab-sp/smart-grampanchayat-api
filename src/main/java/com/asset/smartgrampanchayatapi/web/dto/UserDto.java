package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserDto", description = "Tenant-scoped user details")
public record UserDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID tenantId,
        String email,
        String phone,
        @Schema(description = "Stored role from users.role", requiredMode = Schema.RequiredMode.REQUIRED) UserRole role,
        @Schema(
                description = "Role for UI / permissions: elevated role while acting window is active, else role",
                requiredMode = Schema.RequiredMode.REQUIRED
        ) UserRole effectiveRole,
        @Schema(description = "Temporary elevated role when acting window is configured") UserRole elevatedRole,
        @Schema(description = "Start of elevation window (inclusive)") Instant actingFrom,
        @Schema(description = "End of elevation window (exclusive)") Instant actingUntil,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String firstName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String lastName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean active,
        Instant lastLoginAt
) {
    public static UserDto fromEntity(ShardUser user) {
        Instant now = Instant.now();
        return new UserDto(
                user.getId(),
                user.getTenantId(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.effectiveRoleAt(now),
                user.getElevatedRole(),
                user.getActingFrom(),
                user.getActingUntil(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getLastLoginAt()
        );
    }
}
