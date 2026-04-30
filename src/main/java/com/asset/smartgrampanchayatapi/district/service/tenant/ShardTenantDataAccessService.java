package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.jpa.repository.ShardTenantRepository;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;

@Service
public class ShardTenantDataAccessService {

    private final ShardTenantRepository shardTenantRepository;

    public ShardTenantDataAccessService(ShardTenantRepository shardTenantRepository) {
        this.shardTenantRepository = shardTenantRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<ShardTenant> findByTenantCode(District district, String tenantCode) {
        return shardTenantRepository.findByTenantCode(tenantCode);
    }
}
