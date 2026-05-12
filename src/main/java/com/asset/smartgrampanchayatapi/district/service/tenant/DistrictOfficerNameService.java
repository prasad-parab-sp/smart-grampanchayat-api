package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.asset.smartgrampanchayatapi.district.jpa.model.Grampanchayat;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardUser;
import com.asset.smartgrampanchayatapi.district.jpa.model.UserRole;
import com.asset.smartgrampanchayatapi.district.jpa.repository.GrampanchayatRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.ShardUserRepository;

/**
 * Resolves officer display names for certificates and tenant profile: sarpanch may appear only via
 * {@link ShardUser#effectiveRoleAt(java.time.Instant)} (acting), gramsevak falls back to the active GRAMSEVAK user
 * when {@code grampanchayat.gramsevak_name} is blank.
 */
@Service
public class DistrictOfficerNameService {

    private final ShardUserRepository shardUserRepository;
    private final GrampanchayatRepository grampanchayatRepository;

    public DistrictOfficerNameService(ShardUserRepository shardUserRepository, GrampanchayatRepository grampanchayatRepository) {
        this.shardUserRepository = shardUserRepository;
        this.grampanchayatRepository = grampanchayatRepository;
    }

    public Optional<String> resolveSarpanchDisplayName(UUID tenantId) {
        return resolveSarpanchDisplayName(tenantId, Instant.now());
    }

    public Optional<String> resolveSarpanchDisplayName(UUID tenantId, Instant at) {
        Optional<String> direct = shardUserRepository
                .findByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.SARPANCH)
                .map(DistrictOfficerNameService::displayNameFromUser)
                .filter(s -> !s.isBlank());
        if (direct.isPresent()) {
            return direct;
        }
        return shardUserRepository.findAllByTenantId(tenantId).stream()
                .filter(ShardUser::isActive)
                .filter(u -> u.effectiveRoleAt(at) == UserRole.SARPANCH)
                .map(DistrictOfficerNameService::displayNameFromUser)
                .filter(s -> !s.isBlank())
                .findFirst();
    }

    public Optional<String> resolveGramsevakDisplayName(UUID tenantId) {
        Optional<String> fromGp = grampanchayatRepository
                .findByTenantId(tenantId)
                .map(Grampanchayat::getGramsevakName)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
        if (fromGp.isPresent()) {
            return fromGp;
        }
        return shardUserRepository
                .findByTenantIdAndRoleAndActiveTrue(tenantId, UserRole.GRAMSEVAK)
                .map(DistrictOfficerNameService::displayNameFromUser)
                .filter(s -> !s.isBlank());
    }

    private static String displayNameFromUser(ShardUser u) {
        String f = u.getFirstName() != null ? u.getFirstName().trim() : "";
        String l = u.getLastName() != null ? u.getLastName().trim() : "";
        String joined = (f + " " + l).trim();
        return joined.isEmpty() ? "" : joined;
    }
}
