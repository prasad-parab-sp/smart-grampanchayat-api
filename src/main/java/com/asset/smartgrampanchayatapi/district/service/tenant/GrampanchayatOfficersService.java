package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.web.dto.GrampanchayatOfficersUpdateRequest;

@Service
public class GrampanchayatOfficersService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final GrampanchayatOfficersDataAccessService grampanchayatOfficersDataAccessService;

    public GrampanchayatOfficersService(
            TenantShardRoutingService tenantShardRoutingService,
            GrampanchayatOfficersDataAccessService grampanchayatOfficersDataAccessService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.grampanchayatOfficersDataAccessService = grampanchayatOfficersDataAccessService;
    }

    public void updateOfficers(GrampanchayatOfficersUpdateRequest req) {
        tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not update grampanchayat officers",
                        ctx -> {
                            grampanchayatOfficersDataAccessService.updateOfficers(ctx, req);
                            return Optional.of(Boolean.TRUE);
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant: no master DB row for tenant_code matching X-Tenant-Code."
                ));
    }
}
