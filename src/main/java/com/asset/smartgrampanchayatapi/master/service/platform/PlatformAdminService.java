package com.asset.smartgrampanchayatapi.master.service.platform;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.common.security.PasswordHashVerifier;
import com.asset.smartgrampanchayatapi.common.util.MobileNumberNormalizer;
import com.asset.smartgrampanchayatapi.master.jpa.model.SuperAdmin;
import com.asset.smartgrampanchayatapi.master.jpa.repository.DistrictRepository;
import com.asset.smartgrampanchayatapi.master.jpa.repository.MasterTenantRepository;
import com.asset.smartgrampanchayatapi.master.jpa.repository.SuperAdminRepository;
import com.asset.smartgrampanchayatapi.web.dto.PlatformAdminLoginResponse;
import com.asset.smartgrampanchayatapi.web.dto.PlatformStatsDto;

@Service
public class PlatformAdminService {

    private static final String STATUS_ACTIVE = "active";

    private final SuperAdminRepository superAdminRepository;
    private final DistrictRepository districtRepository;
    private final MasterTenantRepository masterTenantRepository;

    public PlatformAdminService(
            SuperAdminRepository superAdminRepository,
            DistrictRepository districtRepository,
            MasterTenantRepository masterTenantRepository
    ) {
        this.superAdminRepository = superAdminRepository;
        this.districtRepository = districtRepository;
        this.masterTenantRepository = masterTenantRepository;
    }

    @Transactional(transactionManager = "masterTransactionManager")
    public PlatformAdminLoginResponse login(String mobile, String password) {
        String normalizedMobile = MobileNumberNormalizer.normalize(mobile);
        if (!MobileNumberNormalizer.isValidTenDigit(normalizedMobile)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid mobile number");
        }
        if (password == null || password.isBlank()) {
            throw unauthorized();
        }

        SuperAdmin admin = superAdminRepository.findByMobile(normalizedMobile)
                .orElseThrow(this::unauthorized);

        if (!admin.isActive()) {
            throw unauthorized();
        }
        String storedHash = admin.getPasswordHash();
        if (storedHash == null || storedHash.isBlank()) {
            throw unauthorized();
        }
        if (!PasswordHashVerifier.matches(password, storedHash)) {
            throw unauthorized();
        }

        Instant now = Instant.now();
        admin.setLastLoginAt(now);
        admin.setUpdatedAt(now);
        superAdminRepository.save(admin);

        return new PlatformAdminLoginResponse(
                admin.getId().toString(),
                admin.getMobile(),
                admin.getName(),
                "super_admin"
        );
    }

    @Transactional(transactionManager = "masterTransactionManager", readOnly = true)
    public PlatformStatsDto stats() {
        long districtsTotal = districtRepository.count();
        long districtsActive = districtRepository.countByStatusIgnoreCase(STATUS_ACTIVE);
        long gramPanchayatsTotal = masterTenantRepository.count();
        long gramPanchayatsActive = masterTenantRepository.countByStatusIgnoreCase(STATUS_ACTIVE);
        return new PlatformStatsDto(districtsTotal, districtsActive, gramPanchayatsTotal, gramPanchayatsActive);
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid platform credentials");
    }
}
