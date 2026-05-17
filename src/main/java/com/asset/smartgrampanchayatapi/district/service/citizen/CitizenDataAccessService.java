package com.asset.smartgrampanchayatapi.district.service.citizen;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenStatus;
import com.asset.smartgrampanchayatapi.district.jpa.model.GenderType;
import com.asset.smartgrampanchayatapi.district.jpa.model.Grampanchayat;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CitizenRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.GrampanchayatRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.CitizenDto;
import com.asset.smartgrampanchayatapi.web.dto.CitizenRegisterResponse;
import com.asset.smartgrampanchayatapi.web.dto.CitizenStatsDto;
import com.asset.smartgrampanchayatapi.web.dto.CitizenUpsertRequest;

@Service
public class CitizenDataAccessService {

    private final CitizenRepository citizenRepository;
    private final GrampanchayatRepository grampanchayatRepository;

    public CitizenDataAccessService(
            CitizenRepository citizenRepository,
            GrampanchayatRepository grampanchayatRepository
    ) {
        this.citizenRepository = citizenRepository;
        this.grampanchayatRepository = grampanchayatRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<Citizen> findByTenantIdAndMobile(UUID tenantId, String mobile) {
        return citizenRepository.findByTenantIdAndMobile(tenantId, mobile);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<Citizen> findByTenantIdAndEmailIgnoreCase(UUID tenantId, String email) {
        return citizenRepository.findByTenantIdAndEmailIgnoreCase(tenantId, email);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<Citizen> findByIdAndTenantId(UUID citizenId, UUID tenantId) {
        return citizenRepository.findByIdAndTenantId(citizenId, tenantId)
                .filter(c -> c.getDeletedAt() == null);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public CitizenRegisterResponse listRegister(
            UUID tenantId,
            String search,
            GenderType gender,
            CitizenRegisterFilter filter
    ) {
        List<Citizen> all = citizenRepository.findAllByTenantIdAndDeletedAtIsNullOrderByLastNameAscFirstNameAsc(
                tenantId);
        String q = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
        List<CitizenDto> filtered = new ArrayList<>();
        for (Citizen row : all) {
            if (!matchesGender(row, gender)) {
                continue;
            }
            if (!matchesFilter(row, filter)) {
                continue;
            }
            if (!q.isEmpty() && !matchesSearch(row, q)) {
                continue;
            }
            filtered.add(CitizenDto.fromEntity(row));
        }
        filtered.sort(Comparator.comparing(CitizenDto::lastName, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CitizenDto::firstName, String.CASE_INSENSITIVE_ORDER));
        return new CitizenRegisterResponse(filtered, computeStats(all));
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CitizenDto insertCitizen(TenantShardRoutingContext ctx, CitizenUpsertRequest body) {
        Grampanchayat gp = grampanchayatRepository
                .findByTenantId(ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Gram panchayat profile is not configured for this tenant."
                ));
        String mobile = normalizeMobile(body.mobile());
        if (mobile != null) {
            citizenRepository.findByTenantIdAndMobile(ctx.tenantId(), mobile).ifPresent(existing -> {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A citizen with this mobile number already exists."
                );
            });
        }
        Instant now = Instant.now();
        Citizen row = Citizen.newRow();
        row.setId(UUID.randomUUID());
        row.setTenantId(ctx.tenantId());
        row.setGrampanchayatId(gp.getId());
        row.setCitizenUid(nextCitizenUid(ctx.tenantCode(), ctx.tenantId(), citizenRepository));
        applyUpsert(row, body, mobile);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return CitizenDto.fromEntity(citizenRepository.save(row));
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CitizenDto updateCitizen(UUID tenantId, UUID citizenId, CitizenUpsertRequest body) {
        Citizen row = citizenRepository.findByIdAndTenantId(citizenId, tenantId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen not found."));
        String mobile = normalizeMobile(body.mobile());
        if (mobile != null) {
            citizenRepository.findByTenantIdAndMobile(tenantId, mobile).ifPresent(existing -> {
                if (!existing.getId().equals(citizenId)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A citizen with this mobile number already exists."
                    );
                }
            });
        }
        applyUpsert(row, body, mobile);
        row.setUpdatedAt(Instant.now());
        return CitizenDto.fromEntity(citizenRepository.save(row));
    }

    private void applyUpsert(Citizen row, CitizenUpsertRequest body, String mobile) {
        row.setFirstName(body.firstName().trim());
        row.setMiddleName(trimOrNull(body.middleName()));
        row.setLastName(body.lastName().trim());
        row.setDateOfBirth(body.dateOfBirth());
        row.setGender(body.gender());
        row.setAddressLine1(trimOrNull(body.addressLine1()));
        row.setWardNumber(trimOrNull(body.wardNumber()));
        row.setMobile(mobile);
        row.setVoterId(trimOrNull(body.voterId()));
        row.setRationCardNumber(trimOrNull(body.rationCardNumber()));
        row.setBpl(body.bpl());
        row.setBplCardNumber(trimOrNull(body.bplCardNumber()));
        row.setAnnualIncome(body.annualIncome());
        row.setDisabled(body.disabled());
        row.setDisabilityType(trimOrNull(body.disabilityType()));
        row.setStatus(body.status());
    }

    private static String nextCitizenUid(String tenantCode, UUID tenantId, CitizenRepository citizenRepository) {
        long seq = citizenRepository.countByTenantIdAndDeletedAtIsNull(tenantId) + 1;
        String code = tenantCode == null ? "GP" : tenantCode.trim().toUpperCase(Locale.ROOT);
        return code + "-C" + String.format(Locale.ROOT, "%06d", seq);
    }

    private static String normalizeMobile(String mobile) {
        if (mobile == null) {
            return null;
        }
        String trimmed = mobile.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String trimOrNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean matchesGender(Citizen row, GenderType gender) {
        return gender == null || row.getGender() == gender;
    }

    private static boolean matchesFilter(Citizen row, CitizenRegisterFilter filter) {
        if (filter == null || filter == CitizenRegisterFilter.all) {
            return true;
        }
        return switch (filter) {
            case all -> true;
            case active -> row.getStatus() == CitizenStatus.active;
            case voter -> row.getStatus() == CitizenStatus.active
                    && row.getVoterId() != null
                    && !row.getVoterId().isBlank();
            case bpl -> row.getStatus() == CitizenStatus.active && row.isBpl();
            case disabled -> row.getStatus() == CitizenStatus.active && row.isDisabled();
            case deceased -> row.getStatus() == CitizenStatus.deceased;
            case migrated -> row.getStatus() == CitizenStatus.migrated;
        };
    }

    private static boolean matchesSearch(Citizen row, String q) {
        return contains(row.getFirstName(), q)
                || contains(row.getMiddleName(), q)
                || contains(row.getLastName(), q)
                || contains(row.getMobile(), q)
                || contains(row.getVoterId(), q)
                || contains(row.getAddressLine1(), q)
                || contains(row.getWardNumber(), q)
                || contains(row.getCitizenUid(), q);
    }

    private static boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q);
    }

    private static CitizenStatsDto computeStats(List<Citizen> all) {
        int active = 0;
        int voters = 0;
        int male = 0;
        int female = 0;
        int bpl = 0;
        int disabled = 0;
        int deceased = 0;
        int migrated = 0;
        for (Citizen row : all) {
            if (row.getStatus() == CitizenStatus.deceased) {
                deceased++;
            } else if (row.getStatus() == CitizenStatus.migrated) {
                migrated++;
            }
            if (row.getStatus() != CitizenStatus.active) {
                continue;
            }
            active++;
            if (row.getGender() == GenderType.male) {
                male++;
            } else if (row.getGender() == GenderType.female) {
                female++;
            }
            if (row.getVoterId() != null && !row.getVoterId().isBlank()) {
                voters++;
            }
            if (row.isBpl()) {
                bpl++;
            }
            if (row.isDisabled()) {
                disabled++;
            }
        }
        return new CitizenStatsDto(active, voters, male, female, bpl, disabled, deceased, migrated);
    }
}
