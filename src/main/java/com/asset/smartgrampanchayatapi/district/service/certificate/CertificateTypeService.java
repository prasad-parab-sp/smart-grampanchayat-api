package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateType;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeCategory;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeField;
import com.asset.smartgrampanchayatapi.district.jpa.model.TenantCertificateTypeConfig;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeFieldDto;
import com.asset.smartgrampanchayatapi.web.dto.CertificateTypeUpsertRequest;

@Service
public class CertificateTypeService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CertificateTypeDataAccessService certificateTypeDataAccessService;

    public CertificateTypeService(
            TenantShardRoutingService tenantShardRoutingService,
            CertificateTypeDataAccessService certificateTypeDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.certificateTypeDataAccessService = certificateTypeDataAccessService;
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
     */
    public CertificateTypeDto createCertificateType(CertificateTypeUpsertRequest body) {
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
     * Tenant override when a config row exists; otherwise the platform default from {@link CertificateType}.
     */
    private static BigDecimal resolveFeeAmount(CertificateType ct, TenantCertificateTypeConfig tenantConfig) {
        if (tenantConfig != null) {
            return tenantConfig.getFeeAmount();
        }
        return ct.getDefaultFeeAmount();
    }
}
