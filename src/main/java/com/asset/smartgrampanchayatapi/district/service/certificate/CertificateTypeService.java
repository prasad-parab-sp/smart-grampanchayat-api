package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeFieldDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeUpsertRequest;

@Service
public class CertificateTypeService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CertificateTypeDataAccessService certificateTypeDataAccessService;
    private final UserService userService;

    public CertificateTypeService(
            TenantShardRoutingService tenantShardRoutingService,
            CertificateTypeDataAccessService certificateTypeDataAccessService,
            UserService userService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.certificateTypeDataAccessService = certificateTypeDataAccessService;
        this.userService = userService;
    }

    /**
     * Finds {@link CertificateType} rows visible to the tenant from {@link TenantCodeContext}, mapped to {@link CertificateTypeDto},
     * with {@link CertificateTypeDto#feeAmount} and optional {@link CertificateTypeDto#tenantCertificateTypeConfig}
     * populated using server-side rules in this service.
     *
     * @param category optional filter ({@link CertificateTypeCategory}); all groups when null
     * @return empty if the tenant code does not resolve to a shard; otherwise ordered rows
     */
    public Optional<List<CertificateTypeDto>> findVisibleCertificateTypesForTenant(CertificateTypeCategory category) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load certificate types from district database",
                ctx -> {
                    List<CertificateType> types = certificateTypeDataAccessService.findVisibleCertificateTypesForTenant(
                            ctx.tenantId(), category);
                    if (types.isEmpty()) {
                        return Optional.of(List.of());
                    }
                    List<UUID> ids = types.stream().map(CertificateType::getId).toList();
                    Map<UUID, TenantCertificateTypeConfig> tenantConfigs =
                            certificateTypeDataAccessService.findTenantCertificateTypeConfigs(ctx.tenantId(), ids);
                    var fieldsByCertType =
                            certificateTypeDataAccessService.findCertificateTypeFieldsByCertificateTypeIds(ids);
                    List<CertificateTypeDto> items = types.stream()
                            .map(ct -> {
                                TenantCertificateTypeConfig cfg = tenantConfigs.get(ct.getId());
                                BigDecimal feeAmount = resolveFeeAmount(ct, cfg);
                                List<CertificateTypeFieldDto> extraFields = fieldsByCertType
                                        .getOrDefault(ct.getId(), List.of()).stream()
                                        .map(CertificateTypeFieldDto::fromEntity)
                                        .toList();
                                return CertificateTypeDto.fromCertificateTypeAndTenantConfig(
                                        ct, cfg, feeAmount, extraFields);
                            })
                            .toList();
                    return Optional.of(items);
                }
        );
    }

    /**
     * Persists a new tenant-scoped {@code certificate_type} row (and optional {@code certificate_type_field} rows).
     * Caller must prove staff identity; only {@link UserRole#GP_ADMIN} or {@link UserRole#SYS_ADMIN}
     * ({@linkplain ShardUser#effectiveRoleAt(java.time.Instant) effective role}) may create types.
     */
    public CertificateTypeDto createCertificateType(CertificateTypeCreateRequest request) {
        authorizeCertificateTypeWrite(request.identifier(), request.password());
        CertificateTypeUpsertRequest body = request.certificateType();
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not create certificate type on district database",
                        ctx -> Optional.of(certificateTypeDataAccessService.insertTenantCertificateType(ctx, body))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code."
                ));
    }

    /**
     * Lists tenant-owned certificate types for GP Admin / System Admin (includes inactive rows).
     */
    public List<CertificateTypeDto> listTenantOwnedCertificateTypes() {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not list tenant certificate types from district database",
                        ctx -> {
                            List<CertificateType> types =
                                    certificateTypeDataAccessService.findTenantOwnedCertificateTypes(ctx.tenantId());
                            if (types.isEmpty()) {
                                return Optional.of(List.<CertificateTypeDto>of());
                            }
                            return Optional.of(mapTypesToDtos(ctx.tenantId(), types));
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code."
                ));
    }

    /**
     * Loads one tenant-owned certificate type by id (includes inactive).
     */
    public CertificateTypeDto getTenantOwnedCertificateTypeById(UUID id) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not load tenant certificate type from district database",
                        ctx -> certificateTypeDataAccessService
                                .findTenantOwnedCertificateTypeById(ctx.tenantId(), id)
                                .map(ct -> toDto(ctx.tenantId(), ct))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant certificate type not found."
                ));
    }

    /**
     * Updates a tenant-owned certificate type. Catalog {@code code} is immutable.
     */
    public CertificateTypeDto updateCertificateType(UUID id, CertificateTypeUpsertRequest body) {
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not update certificate type on district database",
                        ctx -> Optional.of(certificateTypeDataAccessService.updateTenantCertificateType(ctx, id, body))
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code."
                ));
    }

    private void authorizeCertificateTypeWrite(String identifier, String password) {
        ShardUser staff = userService
                .verifyActiveUserCredentials(identifier, password)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials."));
        Instant now = Instant.now();
        UserRole effective = staff.effectiveRoleAt(now);
        if (effective != UserRole.GP_ADMIN && effective != UserRole.SYS_ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only GP Admin or System Admin may manage certificate types."
            );
        }
    }

    private List<CertificateTypeDto> mapTypesToDtos(UUID tenantId, List<CertificateType> types) {
        List<UUID> ids = types.stream().map(CertificateType::getId).toList();
        Map<UUID, TenantCertificateTypeConfig> tenantConfigs =
                certificateTypeDataAccessService.findTenantCertificateTypeConfigs(tenantId, ids);
        var fieldsByCertType = certificateTypeDataAccessService.findCertificateTypeFieldsByCertificateTypeIds(ids);
        return types.stream()
                .map(ct -> {
                    TenantCertificateTypeConfig cfg = tenantConfigs.get(ct.getId());
                    BigDecimal feeAmount = resolveFeeAmount(ct, cfg);
                    List<CertificateTypeFieldDto> extraFields = fieldsByCertType
                            .getOrDefault(ct.getId(), List.of()).stream()
                            .map(CertificateTypeFieldDto::fromEntity)
                            .toList();
                    return CertificateTypeDto.fromCertificateTypeAndTenantConfig(ct, cfg, feeAmount, extraFields);
                })
                .toList();
    }

    private CertificateTypeDto toDto(UUID tenantId, CertificateType ct) {
        Map<UUID, TenantCertificateTypeConfig> tenantConfigs =
                certificateTypeDataAccessService.findTenantCertificateTypeConfigs(tenantId, List.of(ct.getId()));
        var fieldsByCertType =
                certificateTypeDataAccessService.findCertificateTypeFieldsByCertificateTypeIds(List.of(ct.getId()));
        TenantCertificateTypeConfig cfg = tenantConfigs.get(ct.getId());
        BigDecimal feeAmount = resolveFeeAmount(ct, cfg);
        List<CertificateTypeFieldDto> extraFields = fieldsByCertType
                .getOrDefault(ct.getId(), List.of()).stream()
                .map(CertificateTypeFieldDto::fromEntity)
                .toList();
        return CertificateTypeDto.fromCertificateTypeAndTenantConfig(ct, cfg, feeAmount, extraFields);
    }

    /**
     * Tenant override when a config row exists; otherwise the platform default from {@link CertificateType}.
     */
    private static BigDecimal resolveFeeAmount(CertificateType ct, TenantCertificateTypeConfig tenantConfig) {
        if (tenantConfig != null) {
            return tenantConfig.getFeeAmount();
        }
        return ct.getDefaultFeeAmount();
    }
}
