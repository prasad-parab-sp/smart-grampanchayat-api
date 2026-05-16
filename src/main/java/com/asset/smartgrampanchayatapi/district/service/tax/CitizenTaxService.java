package com.asset.smartgrampanchayatapi.district.service.tax;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTaxStatus;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.citizen.CitizenDataAccessService;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxDto;
import com.asset.smartgrampanchayatapi.web.dto.TaxPaymentCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TaxPaymentDto;

@Service
public class CitizenTaxService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final CitizenTaxDataAccessService citizenTaxDataAccessService;
    private final CitizenDataAccessService citizenDataAccessService;
    private final UserService userService;

    public CitizenTaxService(
            TenantShardRoutingService tenantShardRoutingService,
            CitizenTaxDataAccessService citizenTaxDataAccessService,
            CitizenDataAccessService citizenDataAccessService,
            UserService userService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.citizenTaxDataAccessService = citizenTaxDataAccessService;
        this.citizenDataAccessService = citizenDataAccessService;
        this.userService = userService;
    }

    public Optional<List<CitizenTaxDto>> listCitizenTaxes(UUID citizenId) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load citizen taxes from district database",
                ctx -> {
                    requireCitizen(ctx.tenantId(), citizenId);
                    return Optional.of(citizenTaxDataAccessService.listForCitizen(ctx.tenantId(), citizenId));
                }
        );
    }

    public Optional<List<CitizenTaxDto>> listTenantTaxes(CitizenTaxStatus status, String financialYear) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load taxes from district database",
                ctx -> Optional.of(citizenTaxDataAccessService.listForTenant(ctx.tenantId(), status, financialYear))
        );
    }

    public Optional<CitizenTaxDto> getCitizenTax(UUID id) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load citizen tax from district database",
                ctx -> citizenTaxDataAccessService.findById(ctx.tenantId(), id)
        );
    }

    public CitizenTaxDto createCitizenTax(UUID citizenId, CitizenTaxCreateRequest request) {
        userService.verifyActiveStaffForTaxWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not create citizen tax on district database",
                        ctx -> {
                            requireCitizen(ctx.tenantId(), citizenId);
                            return Optional.of(
                                    citizenTaxDataAccessService.insertCitizenTax(
                                            ctx,
                                            citizenId,
                                            request.staffUserId(),
                                            request
                                    )
                            );
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    public Optional<List<TaxPaymentDto>> listPayments(UUID citizenTaxId) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load tax payments from district database",
                ctx -> {
                    if (citizenTaxDataAccessService.findById(ctx.tenantId(), citizenTaxId).isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen tax not found.");
                    }
                    return Optional.of(citizenTaxDataAccessService.listPayments(ctx.tenantId(), citizenTaxId));
                }
        );
    }

    public TaxPaymentDto recordPayment(UUID citizenTaxId, TaxPaymentCreateRequest request) {
        userService.verifyActiveStaffForTaxWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not record tax payment on district database",
                        ctx -> {
                            if (citizenTaxDataAccessService.findById(ctx.tenantId(), citizenTaxId).isEmpty()) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen tax not found.");
                            }
                            return Optional.of(
                                    citizenTaxDataAccessService.recordPayment(
                                            ctx.tenantId(),
                                            citizenTaxId,
                                            request.staffUserId(),
                                            request
                                    )
                            );
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    private void requireCitizen(UUID tenantId, UUID citizenId) {
        if (citizenDataAccessService.findByIdAndTenantId(citizenId, tenantId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen not found for this tenant.");
        }
    }
}
