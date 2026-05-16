package com.asset.smartgrampanchayatapi.district.service.tax;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asset.smartgrampanchayatapi.district.jpa.model.TaxType;
import com.asset.smartgrampanchayatapi.district.jpa.repository.TaxTypeRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeDto;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeUpsertRequest;

@Service
public class TaxTypeDataAccessService {

    private final TaxTypeRepository taxTypeRepository;

    public TaxTypeDataAccessService(TaxTypeRepository taxTypeRepository) {
        this.taxTypeRepository = taxTypeRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<TaxTypeDto> listTaxTypes(UUID tenantId, boolean activeOnly) {
        List<TaxType> rows = activeOnly
                ? taxTypeRepository.findByTenantIdAndActiveTrueOrderByNameEnAsc(tenantId)
                : taxTypeRepository.findByTenantIdOrderByNameEnAsc(tenantId);
        return rows.stream().map(TaxTypeDto::fromEntity).toList();
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<TaxTypeDto> findTaxType(UUID tenantId, UUID id) {
        return taxTypeRepository.findByTenantIdAndId(tenantId, id).map(TaxTypeDto::fromEntity);
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<TaxType> findTaxTypeEntity(UUID tenantId, UUID id) {
        return taxTypeRepository.findByTenantIdAndId(tenantId, id);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public TaxTypeDto insertTaxType(TenantShardRoutingContext ctx, TaxTypeUpsertRequest body) {
        Instant now = Instant.now();
        TaxType row = new TaxType();
        row.setId(UUID.randomUUID());
        row.setTenantId(ctx.tenantId());
        applyUpsert(row, body);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return TaxTypeDto.fromEntity(taxTypeRepository.save(row));
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public TaxTypeDto updateTaxType(UUID tenantId, UUID id, TaxTypeUpsertRequest body) {
        TaxType row = taxTypeRepository
                .findByTenantIdAndId(tenantId, id)
                .orElseThrow();
        applyUpsert(row, body);
        row.setUpdatedAt(Instant.now());
        return TaxTypeDto.fromEntity(taxTypeRepository.save(row));
    }

    private static void applyUpsert(TaxType row, TaxTypeUpsertRequest body) {
        row.setNameEn(body.nameEn().trim());
        row.setNameMr(body.nameMr().trim());
        row.setDescription(blankToNull(body.description()));
        if (body.active() != null) {
            row.setActive(body.active());
        }
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
