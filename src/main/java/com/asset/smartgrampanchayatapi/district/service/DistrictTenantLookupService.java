package com.asset.smartgrampanchayatapi.district.service;

import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.routing.DistrictRoutingHolder;
import com.asset.smartgrampanchayatapi.exception.DistrictShardUnavailableException;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.asset.smartgrampanchayatapi.master.jpa.model.MasterTenant;
import com.asset.smartgrampanchayatapi.master.jpa.repository.MasterTenantRepository;

@Service
public class DistrictTenantLookupService {

    private final MasterTenantRepository masterTenantRepository;
    private final DistrictShardTenantQueryService districtShardTenantQueryService;

    public DistrictTenantLookupService(
            MasterTenantRepository masterTenantRepository,
            DistrictShardTenantQueryService districtShardTenantQueryService
    ) {
        this.masterTenantRepository = masterTenantRepository;
        this.districtShardTenantQueryService = districtShardTenantQueryService;
    }

    /**
     * Resolves {@code tenantCode} on the master DB, then loads the full row from that district's {@code tenants} table.
     */
    public Optional<ShardTenant> findByTenantCode(String tenantCode) {
        Optional<MasterTenant> master = masterTenantRepository.findByTenantCode(tenantCode);
        if (master.isEmpty()) {
            return Optional.empty();
        }
        District district = master.get().getDistrict();
        if (district == null) {
            return Optional.empty();
        }
        // Must bind before @Transactional runs on the shard service: the transaction opens a JDBC
        // connection before the proxied method body executes; DistrictRoutingDataSource needs this.
        DistrictRoutingHolder.bind(district);
        try {
            return districtShardTenantQueryService.findByTenantCode(district, tenantCode);
        } catch (DataAccessException e) {
            throw new DistrictShardUnavailableException("Could not load tenant from district database", e);
        } finally {
            DistrictRoutingHolder.clear();
        }
    }
}
