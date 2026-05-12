package com.asset.smartgrampanchayatapi.district.service.tenant;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.Grampanchayat;
import com.asset.smartgrampanchayatapi.district.jpa.repository.GrampanchayatRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.GrampanchayatOfficersUpdateRequest;

@Service
public class GrampanchayatOfficersDataAccessService {

    private final GrampanchayatRepository grampanchayatRepository;

    public GrampanchayatOfficersDataAccessService(GrampanchayatRepository grampanchayatRepository) {
        this.grampanchayatRepository = grampanchayatRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public void updateOfficers(TenantShardRoutingContext ctx, GrampanchayatOfficersUpdateRequest req) {
        Grampanchayat gp = grampanchayatRepository
                .findByTenantId(ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No grampanchayat row for this tenant. Create or seed grampanchayat first."
                ));
        Instant now = Instant.now();
        if (req.gramsevakName() != null) {
            gp.setGramsevakName(trimToNull(req.gramsevakName()));
        }
        gp.setUpdatedAt(now);
        grampanchayatRepository.save(gp);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
