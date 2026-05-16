package com.asset.smartgrampanchayatapi.district.service.tax;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypePatchRequest;

@Service
public class TaxTypeService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final TaxTypeDataAccessService taxTypeDataAccessService;
    private final UserService userService;

    public TaxTypeService(
            TenantShardRoutingService tenantShardRoutingService,
            TaxTypeDataAccessService taxTypeDataAccessService,
            UserService userService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.taxTypeDataAccessService = taxTypeDataAccessService;
        this.userService = userService;
    }

    public Optional<List<TaxTypeDto>> listTaxTypes(boolean activeOnly) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load tax types from district database",
                ctx -> Optional.of(taxTypeDataAccessService.listTaxTypes(ctx.tenantId(), activeOnly))
        );
    }

    public Optional<TaxTypeDto> getTaxType(UUID id) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load tax type from district database",
                ctx -> taxTypeDataAccessService.findTaxType(ctx.tenantId(), id)
        );
    }

    public TaxTypeDto createTaxType(TaxTypeCreateRequest request) {
        userService.verifyActiveStaffForTaxCatalogWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not create tax type on district database",
                        ctx -> Optional.of(
                                taxTypeDataAccessService.insertTaxType(ctx, request.taxType())
                        )
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    public TaxTypeDto updateTaxType(UUID id, TaxTypePatchRequest request) {
        userService.verifyActiveStaffForTaxCatalogWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not update tax type on district database",
                        ctx -> {
                            if (taxTypeDataAccessService.findTaxType(ctx.tenantId(), id).isEmpty()) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Tax type not found.");
                            }
                            return Optional.of(
                                    taxTypeDataAccessService.updateTaxType(
                                            ctx.tenantId(),
                                            id,
                                            request.taxType()
                                    )
                            );
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }
}
