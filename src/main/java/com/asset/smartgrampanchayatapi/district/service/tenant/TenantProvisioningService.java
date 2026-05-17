package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.routing.DistrictRoutingHolder;
import com.asset.smartgrampanchayatapi.exception.DistrictShardUnavailableException;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.asset.smartgrampanchayatapi.master.jpa.repository.DistrictRepository;
import com.asset.smartgrampanchayatapi.master.jpa.repository.MasterTenantRepository;
import com.asset.smartgrampanchayatapi.master.service.tenant.MasterTenantDataAccessService;
import com.asset.smartgrampanchayatapi.web.dto.TenantCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TenantProfileDto;

/**
 * Creates a tenant on the master routing database and the matching district shard ({@code tenants} row with the same UUID).
 */
@Service
public class TenantProvisioningService {

    private final DistrictRepository districtRepository;
    private final MasterTenantRepository masterTenantRepository;
    private final MasterTenantDataAccessService masterTenantDataAccessService;
    private final ShardTenantDataAccessService shardTenantDataAccessService;

    public TenantProvisioningService(
            DistrictRepository districtRepository,
            MasterTenantRepository masterTenantRepository,
            MasterTenantDataAccessService masterTenantDataAccessService,
            ShardTenantDataAccessService shardTenantDataAccessService
    ) {
        this.districtRepository = districtRepository;
        this.masterTenantRepository = masterTenantRepository;
        this.masterTenantDataAccessService = masterTenantDataAccessService;
        this.shardTenantDataAccessService = shardTenantDataAccessService;
    }

    public TenantProfileDto createTenant(TenantCreateRequest body) {
        String districtCode = body.districtCode().trim().toUpperCase(Locale.ROOT);
        District district = districtRepository
                .findByDistrictCode(districtCode)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No district found for districtCode: " + districtCode
                ));

        String tenantCode = body.tenantCode().trim().toUpperCase(Locale.ROOT);
        String gpCode = body.gpCode().trim();
        String tenantId = resolveTenantId(body, tenantCode);

        if (masterTenantRepository.existsByTenantCode(tenantCode)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A tenant with this tenantCode already exists on the master database."
            );
        }
        if (masterTenantRepository.existsByTenantId(tenantId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A tenant with this tenantId already exists on the master database."
            );
        }

        DistrictRoutingHolder.bind(district);
        try {
            if (shardTenantDataAccessService.existsByTenantCode(tenantCode)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A tenant with this tenantCode already exists in the district database."
                );
            }
            if (shardTenantDataAccessService.existsByGpCode(gpCode)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A tenant with this gpCode already exists in the district database."
                );
            }
        } finally {
            DistrictRoutingHolder.clear();
        }

        UUID id = UUID.randomUUID();
        masterTenantDataAccessService.insertTenant(id, district, body, tenantCode, tenantId);

        DistrictRoutingHolder.bind(district);
        try {
            var shard = shardTenantDataAccessService.insertTenant(id, district, body, tenantCode, tenantId);
            return TenantProfileDto.fromParts(shard, null, null);
        } catch (DataAccessException e) {
            masterTenantDataAccessService.deleteById(id);
            throw new DistrictShardUnavailableException(
                    "Tenant was not created: district database write failed.",
                    e
            );
        } finally {
            DistrictRoutingHolder.clear();
        }
    }

    private static String resolveTenantId(TenantCreateRequest body, String tenantCode) {
        if (body.tenantId() != null && !body.tenantId().isBlank()) {
            return body.tenantId().trim();
        }
        return tenantCode;
    }
}
