package com.asset.smartgrampanchayatapi.master.service.tenant;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.master.jpa.model.District;
import com.asset.smartgrampanchayatapi.master.jpa.model.MasterTenant;
import com.asset.smartgrampanchayatapi.master.jpa.repository.MasterTenantRepository;
import com.asset.smartgrampanchayatapi.web.dto.TenantCreateRequest;

@Service
public class MasterTenantDataAccessService {

    private final MasterTenantRepository masterTenantRepository;

    public MasterTenantDataAccessService(MasterTenantRepository masterTenantRepository) {
        this.masterTenantRepository = masterTenantRepository;
    }

    @Transactional(transactionManager = "masterTransactionManager")
    public MasterTenant insertTenant(UUID id, District district, TenantCreateRequest body, String tenantCode, String tenantId) {
        MasterTenant row = MasterTenant.newRow();
        row.setId(id);
        row.setTenantCode(tenantCode);
        row.setName(body.name().trim());
        row.setDistrict(district);
        row.setStatus(body.resolvedStatus());
        row.setPlanType(body.resolvedPlanType());
        row.setSubscriptionStartDate(body.subscriptionStartDate());
        row.setSubscriptionEndDate(body.subscriptionEndDate());
        row.setTenantId(tenantId);
        return masterTenantRepository.save(row);
    }

    @Transactional(transactionManager = "masterTransactionManager")
    public void deleteById(UUID id) {
        masterTenantRepository.deleteById(id);
    }
}
