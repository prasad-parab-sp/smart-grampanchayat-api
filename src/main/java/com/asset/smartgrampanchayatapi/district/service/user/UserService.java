package com.asset.smartgrampanchayatapi.district.service.user;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;

@Service
public class UserService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final UserDataAccessService userDataAccessService;

    public UserService(
            TenantShardRoutingService tenantShardRoutingService,
            UserDataAccessService userDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.userDataAccessService = userDataAccessService;
    }

    public Optional<ShardUser> findByMobileOrEmail(String mobile, String email) {
        String code = TenantCodeContext.getRequired();
        if (mobile != null && !mobile.isBlank()) {
            return tenantShardRoutingService.runOnShard(
                    code,
                    "Could not load user from district database",
                    ctx -> userDataAccessService.findByTenantIdAndPhone(ctx.tenantId(), mobile.trim())
            );
        }
        if (email != null && !email.isBlank()) {
            return tenantShardRoutingService.runOnShard(
                    code,
                    "Could not load user from district database",
                    ctx -> userDataAccessService.findByTenantIdAndEmailIgnoreCase(ctx.tenantId(), email.trim())
            );
        }
        return Optional.empty();
    }

    public List<ShardUser> listUsers() {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load users from district database",
                ctx -> Optional.of(userDataAccessService.findAllByTenantId(ctx.tenantId()))
        ).orElse(List.of());
    }

    public Optional<ShardUser> login(String identifier, String password) {
        String value = identifier == null ? "" : identifier.trim();
        if (value.isBlank()) {
            return Optional.empty();
        }
        String normalizedPassword = password == null ? "" : password;
        Optional<ShardUser> user = value.contains("@")
                ? findByMobileOrEmail(null, value)
                : findByMobileOrEmail(value, null);
        if (user.isEmpty()) {
            return Optional.empty();
        }

        ShardUser matched = user.get();
        if (!matched.isActive()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "User is inactive.");
        }
        if (!isPasswordMatch(normalizedPassword, matched.getPasswordHash())) {
            return Optional.empty();
        }

        tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not update user last login from district database",
                ctx -> {
                    matched.setLastLoginAt(Instant.now());
                    return Optional.of(userDataAccessService.save(matched));
                }
        );
        return Optional.of(matched);
    }

    /**
     * Verifies active user credentials on the current tenant shard without updating {@code last_login_at}.
     * Used for sensitive actions (e.g. certificate approval) until a proper staff session/JWT exists.
     */
    public Optional<ShardUser> verifyActiveUserCredentials(String identifier, String password) {
        String value = identifier == null ? "" : identifier.trim();
        if (value.isBlank()) {
            return Optional.empty();
        }
        String normalizedPassword = password == null ? "" : password;
        Optional<ShardUser> user = value.contains("@")
                ? findByMobileOrEmail(null, value)
                : findByMobileOrEmail(value, null);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        ShardUser matched = user.get();
        if (!matched.isActive()) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "User is inactive.");
        }
        if (!isPasswordMatch(normalizedPassword, matched.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(matched);
    }

    /**
     * Verifies an active staff user allowed to manage the tax type catalog (GP admin or gramsevak).
     */
    public ShardUser verifyActiveStaffForTaxCatalogWrite(UUID userId) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not verify staff user on district database",
                        ctx -> {
                            ShardUser user = userDataAccessService
                                    .findByTenantIdAndId(ctx.tenantId(), userId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Unknown staff user."
                                    ));
                            if (!user.isActive()) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive.");
                            }
                            UserRole effective = user.effectiveRoleAt(Instant.now());
                            if (effective == UserRole.VIEWER) {
                                throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Viewers cannot manage tax types."
                                );
                            }
                            if (effective != UserRole.GRAMSEVAK
                                    && effective != UserRole.GP_ADMIN
                                    && effective != UserRole.SYS_ADMIN) {
                                throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Only gramsevak or GP admin can manage tax types."
                                );
                            }
                            return Optional.of(user);
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    /**
     * Verifies an active staff user allowed to manage citizen tax demands and payments.
     */
    public ShardUser verifyActiveStaffForTaxWrite(UUID userId) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not verify staff user on district database",
                        ctx -> {
                            ShardUser user = userDataAccessService
                                    .findByTenantIdAndId(ctx.tenantId(), userId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Unknown staff user."
                                    ));
                            if (!user.isActive()) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive.");
                            }
                            UserRole effective = user.effectiveRoleAt(Instant.now());
                            if (effective == UserRole.VIEWER) {
                                throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Viewers cannot manage taxes."
                                );
                            }
                            if (effective != UserRole.GRAMSEVAK
                                    && effective != UserRole.OPERATOR
                                    && effective != UserRole.SYS_ADMIN) {
                                throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Only gramsevak or operator can manage citizen taxes."
                                );
                            }
                            return Optional.of(user);
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    /**
     * Verifies an active gramsevak or sarpanch may manage the citizen / villager register.
     */
    public ShardUser verifyActiveStaffForCitizenRegisterWrite(UUID userId) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not verify staff user on district database",
                        ctx -> {
                            ShardUser user = userDataAccessService
                                    .findByTenantIdAndId(ctx.tenantId(), userId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Unknown staff user."
                                    ));
                            if (!user.isActive()) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive.");
                            }
                            UserRole effective = user.effectiveRoleAt(Instant.now());
                            if (effective != UserRole.GRAMSEVAK && effective != UserRole.SARPANCH) {
                                throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Only gramsevak or sarpanch can manage the citizen register."
                                );
                            }
                            return Optional.of(user);
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    /**
     * Verifies an active staff user on the current tenant shard by id (no password).
     * Used for notice creation after web admin login; callers must only send the logged-in user's id.
     */
    public ShardUser verifyActiveStaffForNoticeWrite(UUID userId) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not verify staff user on district database",
                        ctx -> {
                            ShardUser user = userDataAccessService
                                    .findByTenantIdAndId(ctx.tenantId(), userId)
                                    .orElseThrow(() -> new ResponseStatusException(
                                            HttpStatus.UNAUTHORIZED,
                                            "Unknown staff user."
                                    ));
                            if (!user.isActive()) {
                                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive.");
                            }
                            UserRole effective = user.effectiveRoleAt(Instant.now());
                            if (effective == UserRole.VIEWER) {
                                throw new ResponseStatusException(
                                        HttpStatus.FORBIDDEN,
                                        "Viewers cannot manage notices."
                                );
                            }
                            return Optional.of(user);
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    /**
     * Sets or clears temporary role elevation for a user in the current tenant shard.
     * Clears when {@code elevatedRole}, {@code actingFrom}, and {@code actingUntil} are all null.
     */
    public ShardUser patchElevation(UUID userId, UserRole elevatedRole, Instant actingFrom, Instant actingUntil) {
        boolean allNull = elevatedRole == null && actingFrom == null && actingUntil == null;
        boolean allSet = elevatedRole != null && actingFrom != null && actingUntil != null;
        if (!allNull && !allSet) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Provide all of elevatedRole, actingFrom, actingUntil, or omit all three to clear elevation."
            );
        }
        if (allSet && !actingFrom.isBefore(actingUntil)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "actingFrom must be before actingUntil.");
        }
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not update user elevation in district database",
                ctx -> {
                    ShardUser user = userDataAccessService
                            .findByTenantIdAndId(ctx.tenantId(), userId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
                    if (allNull) {
                        user.setElevatedRole(null);
                        user.setActingFrom(null);
                        user.setActingUntil(null);
                    } else {
                        user.setElevatedRole(elevatedRole);
                        user.setActingFrom(actingFrom);
                        user.setActingUntil(actingUntil);
                    }
                    return Optional.of(userDataAccessService.save(user));
                }
        ).orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "District database unavailable."));
    }

    private boolean isPasswordMatch(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) {
            return false;
        }
        if (storedHash.startsWith("$2a$") || storedHash.startsWith("$2b$") || storedHash.startsWith("$2y$")) {
            return BCrypt.checkpw(rawPassword, storedHash);
        }
        // Backward-compatible fallback for plain text values used in local/dev data.
        return storedHash.equals(rawPassword);
    }
}
