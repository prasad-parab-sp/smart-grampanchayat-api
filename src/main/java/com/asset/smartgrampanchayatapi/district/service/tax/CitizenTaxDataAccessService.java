package com.asset.smartgrampanchayatapi.district.service.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTax;
import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenTaxStatus;
import com.asset.smartgrampanchayatapi.district.jpa.model.TaxPayment;
import com.asset.smartgrampanchayatapi.district.jpa.model.TaxPaymentMode;
import com.asset.smartgrampanchayatapi.district.jpa.model.TaxType;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CitizenTaxRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.TaxPaymentRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.TaxTypeRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.CitizenTaxDto;
import com.asset.smartgrampanchayatapi.web.dto.TaxPaymentCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.TaxPaymentDto;
import com.asset.smartgrampanchayatapi.web.dto.TaxTypeDto;

@Service
public class CitizenTaxDataAccessService {

    private static final DateTimeFormatter RECEIPT_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CitizenTaxRepository citizenTaxRepository;
    private final TaxTypeRepository taxTypeRepository;
    private final TaxPaymentRepository taxPaymentRepository;

    public CitizenTaxDataAccessService(
            CitizenTaxRepository citizenTaxRepository,
            TaxTypeRepository taxTypeRepository,
            TaxPaymentRepository taxPaymentRepository
    ) {
        this.citizenTaxRepository = citizenTaxRepository;
        this.taxTypeRepository = taxTypeRepository;
        this.taxPaymentRepository = taxPaymentRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<CitizenTaxDto> listForCitizen(UUID tenantId, UUID citizenId) {
        return citizenTaxRepository
                .findByTenantIdAndCitizenIdOrderByDueDateDescCreatedAtDesc(tenantId, citizenId)
                .stream()
                .map(row -> toDto(tenantId, row))
                .toList();
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<CitizenTaxDto> listForTenant(UUID tenantId, CitizenTaxStatus status, String financialYear) {
        String fy = financialYear == null || financialYear.isBlank() ? null : financialYear.trim();
        return citizenTaxRepository
                .findByTenantForList(tenantId, status, fy)
                .stream()
                .map(row -> toDto(tenantId, row))
                .toList();
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<CitizenTaxDto> findById(UUID tenantId, UUID id) {
        return citizenTaxRepository.findByTenantIdAndId(tenantId, id).map(row -> toDto(tenantId, row));
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CitizenTaxDto insertCitizenTax(
            TenantShardRoutingContext ctx,
            UUID citizenId,
            UUID staffUserId,
            CitizenTaxCreateRequest body
    ) {
        TaxType taxType = taxTypeRepository
                .findByTenantIdAndId(ctx.tenantId(), body.taxTypeId())
                .filter(TaxType::isActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax type not found or inactive."));

        String financialYear = normalizeFinancialYear(body.financialYear());
        if (citizenTaxRepository.existsByCitizenIdAndTaxTypeIdAndFinancialYearAndStatusNot(
                citizenId,
                body.taxTypeId(),
                financialYear,
                CitizenTaxStatus.CANCELLED
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A tax demand already exists for this citizen, tax type, and financial year."
            );
        }

        BigDecimal assessed = scaleMoney(body.amountAssessed());
        Instant now = Instant.now();
        CitizenTax row = new CitizenTax();
        row.setId(UUID.randomUUID());
        row.setTenantId(ctx.tenantId());
        row.setCitizenId(citizenId);
        row.setTaxTypeId(taxType.getId());
        row.setFinancialYear(financialYear);
        row.setAssessmentNumber(blankToNull(body.assessmentNumber()));
        row.setAmountAssessed(assessed);
        row.setAmountOutstanding(assessed);
        row.setDueDate(body.dueDate());
        row.setStatus(CitizenTaxStatus.PENDING);
        row.setRemarks(blankToNull(body.remarks()));
        row.setCreatedByUserId(staffUserId);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return toDto(ctx.tenantId(), citizenTaxRepository.save(row));
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<TaxPaymentDto> listPayments(UUID tenantId, UUID citizenTaxId) {
        requireCitizenTax(tenantId, citizenTaxId);
        return taxPaymentRepository
                .findByCitizenTaxIdOrderByPaidOnDescCreatedAtDesc(citizenTaxId)
                .stream()
                .map(TaxPaymentDto::fromEntity)
                .toList();
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public TaxPaymentDto recordPayment(
            UUID tenantId,
            UUID citizenTaxId,
            UUID staffUserId,
            TaxPaymentCreateRequest body
    ) {
        CitizenTax tax = requireCitizenTax(tenantId, citizenTaxId);
        if (tax.getStatus() == CitizenTaxStatus.WAIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot record payment on a waived tax.");
        }
        if (tax.getStatus() == CitizenTaxStatus.CANCELLED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot record payment on a cancelled tax.");
        }
        if (tax.getStatus() == CitizenTaxStatus.PAID) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tax is already fully paid.");
        }

        BigDecimal paymentAmount = scaleMoney(body.amount());
        if (paymentAmount.compareTo(tax.getAmountOutstanding()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment amount exceeds outstanding balance."
            );
        }

        Instant now = Instant.now();
        TaxPayment payment = new TaxPayment();
        payment.setId(UUID.randomUUID());
        payment.setTenantId(tenantId);
        payment.setCitizenTaxId(citizenTaxId);
        payment.setAmount(paymentAmount);
        payment.setPaidOn(body.paidOn());
        payment.setPaymentMode(body.paymentMode());
        payment.setReceiptNumber(generateReceiptNumber(body.paidOn()));
        payment.setReference(blankToNull(body.reference()));
        payment.setRecordedByUserId(staffUserId);
        payment.setCreatedAt(now);
        taxPaymentRepository.save(payment);

        refreshTaxBalances(tax);
        tax.setUpdatedAt(now);
        citizenTaxRepository.save(tax);

        return TaxPaymentDto.fromEntity(payment);
    }

    private CitizenTax requireCitizenTax(UUID tenantId, UUID citizenTaxId) {
        return citizenTaxRepository
                .findByTenantIdAndId(tenantId, citizenTaxId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Citizen tax not found."));
    }

    private void refreshTaxBalances(CitizenTax tax) {
        BigDecimal totalPaid = scaleMoney(taxPaymentRepository.sumAmountByCitizenTaxId(tax.getId()));
        BigDecimal outstanding = tax.getAmountAssessed().subtract(totalPaid);
        if (outstanding.compareTo(BigDecimal.ZERO) < 0) {
            outstanding = BigDecimal.ZERO;
        }
        tax.setAmountOutstanding(outstanding);
        if (outstanding.compareTo(BigDecimal.ZERO) == 0) {
            tax.setStatus(CitizenTaxStatus.PAID);
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            tax.setStatus(CitizenTaxStatus.PARTIAL);
        } else {
            tax.setStatus(CitizenTaxStatus.PENDING);
        }
    }

    private CitizenTaxDto toDto(UUID tenantId, CitizenTax row) {
        TaxTypeDto taxType = taxTypeRepository
                .findByTenantIdAndId(tenantId, row.getTaxTypeId())
                .map(TaxTypeDto::fromEntity)
                .orElse(null);
        return CitizenTaxDto.fromEntity(row, taxType);
    }

    static String normalizeFinancialYear(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (!trimmed.matches("\\d{4}-\\d{2}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "financialYear must match pattern YYYY-YY (e.g. 2025-26)."
            );
        }
        return trimmed;
    }

    static BigDecimal scaleMoney(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private static String generateReceiptNumber(LocalDate paidOn) {
        int suffix = ThreadLocalRandom.current().nextInt(100_000, 1_000_000);
        return "RCP-" + paidOn.format(RECEIPT_DATE) + "-" + suffix;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
