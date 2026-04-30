package com.asset.smartgrampanchayatapi.district.service.citizen;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CitizenRepository;
import com.asset.smartgrampanchayatapi.master.jpa.model.District;

@Service
public class CitizenDataAccessService {

    private final CitizenRepository citizenRepository;

    public CitizenDataAccessService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<Citizen> findByTenantIdAndMobile(District district, UUID tenantId, String mobile) {
        return citizenRepository.findByTenantIdAndMobile(tenantId, mobile);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<Citizen> findByTenantIdAndEmailIgnoreCase(District district, UUID tenantId, String email) {
        return citizenRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<Citizen> findByIdAndTenantId(District district, UUID citizenId, UUID tenantId) {
        return citizenRepository.findByIdAndTenantId(citizenId, tenantId);
    }
}
