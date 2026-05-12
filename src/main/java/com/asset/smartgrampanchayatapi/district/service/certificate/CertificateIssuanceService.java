package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplication;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplicationStatus;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateDocumentFormat;
import com.asset.smartgrampanchayatapi.district.jpa.model.ShardTenant;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateApplicationRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.CertificateDocumentFormatRepository;
import com.asset.smartgrampanchayatapi.district.jpa.repository.ShardTenantRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.district.service.tenant.DistrictOfficerNameService;
import com.asset.smartgrampanchayatapi.web.dto.CertificateIssuedDocumentDto;
import com.asset.smartgrampanchayatapi.web.dto.TenantProfileDto;

@Service
public class CertificateIssuanceService {

    private static final Logger log = LoggerFactory.getLogger(CertificateIssuanceService.class);

    /**
     * Stored HTML is built in Marathi layout on approval; {@link #loadOrBuildIssuedDocument} may still build in
     * another language when cache is empty (e.g. approval-time generation failed).
     */
    private static final String STORED_ISSUED_HTML_LANG = "mr";

    private final CertificateApplicationRepository certificateApplicationRepository;
    private final CertificateDocumentFormatRepository certificateDocumentFormatRepository;
    private final ShardTenantRepository shardTenantRepository;
    private final DistrictOfficerNameService districtOfficerNameService;

    public CertificateIssuanceService(
            CertificateApplicationRepository certificateApplicationRepository,
            CertificateDocumentFormatRepository certificateDocumentFormatRepository,
            ShardTenantRepository shardTenantRepository,
            DistrictOfficerNameService districtOfficerNameService
    ) {
        this.certificateApplicationRepository = certificateApplicationRepository;
        this.certificateDocumentFormatRepository = certificateDocumentFormatRepository;
        this.shardTenantRepository = shardTenantRepository;
        this.districtOfficerNameService = districtOfficerNameService;
    }

    /**
     * Runs in the caller's transaction (e.g. gramsevak approve). Builds printable HTML immediately and persists it.
     * Does not throw — approval must succeed even when no format exists yet (HTML left null for later GET retry).
     */
    public void buildAndPersistIssuedHtmlAfterApproval(UUID tenantId, UUID applicationId) {
        try {
            CertificateApplication app = certificateApplicationRepository
                    .findByIdAndTenantId(applicationId, tenantId)
                    .orElse(null);
            if (app == null || app.getStatus() != CertificateApplicationStatus.APPROVED) {
                return;
            }
            CertificateDocumentFormat format = certificateDocumentFormatRepository
                    .findFirstByTenantIdAndCertificateTypeIdAndActiveTrueOrderByUpdatedAtDesc(tenantId, app.getCertificateTypeId())
                    .orElse(null);
            if (format == null) {
                log.warn(
                        "Approved certificate application {} has no active document format for certificate type {} (tenant {}). HTML not generated.",
                        applicationId,
                        app.getCertificateTypeId(),
                        tenantId
                );
                app.setIssuedDocumentHtml(null);
                certificateApplicationRepository.save(app);
                return;
            }
            ShardTenant tenant = shardTenantRepository.findById(tenantId).orElse(null);
            if (tenant == null) {
                log.warn("Shard tenant row missing for tenantId {} — cannot build issued HTML for application {}.", tenantId, applicationId);
                return;
            }
            TenantProfileDto profile = buildTenantProfile(tenantId, tenant);
            String html = CertificateHtmlExpansionService.buildIssuedHtml(profile, format, STORED_ISSUED_HTML_LANG, app);
            app.setIssuedDocumentHtml(html);
            certificateApplicationRepository.save(app);
        } catch (Exception e) {
            log.warn("Could not build issued certificate HTML for application {} on tenant {}: {}", applicationId, tenantId, e.toString());
            log.debug("Issued HTML build failure", e);
        }
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public CertificateIssuedDocumentDto loadOrBuildIssuedDocument(
            TenantShardRoutingContext ctx,
            UUID applicationId,
            UUID citizenId,
            String langRaw
    ) {
        String lang = normalizeLang(langRaw);
        CertificateApplication app = certificateApplicationRepository
                .findByIdAndTenantId(applicationId, ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found."));
        if (!app.getCitizenId().equals(citizenId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This application does not belong to the given citizen.");
        }
        if (app.getStatus() != CertificateApplicationStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The certificate is only available after the application has been approved."
            );
        }
        String cached = app.getIssuedDocumentHtml();
        if (cached != null && !cached.isBlank()) {
            return new CertificateIssuedDocumentDto(app.getApplicationNumber(), cached);
        }
        CertificateDocumentFormat format = certificateDocumentFormatRepository
                .findFirstByTenantIdAndCertificateTypeIdAndActiveTrueOrderByUpdatedAtDesc(ctx.tenantId(), app.getCertificateTypeId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No active certificate document format is linked to this certificate type. Ask the Gramsevak to publish a format in Admin → Formats."
                ));
        ShardTenant tenant = shardTenantRepository
                .findById(ctx.tenantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tenant row missing on shard."));
        TenantProfileDto profile = buildTenantProfile(ctx.tenantId(), tenant);
        String html = CertificateHtmlExpansionService.buildIssuedHtml(profile, format, lang, app);
        app.setIssuedDocumentHtml(html);
        certificateApplicationRepository.save(app);
        return new CertificateIssuedDocumentDto(app.getApplicationNumber(), html);
    }

    private TenantProfileDto buildTenantProfile(UUID tenantId, ShardTenant tenant) {
        String sarpanch = districtOfficerNameService.resolveSarpanchDisplayName(tenantId).orElse(null);
        String gramsevak = districtOfficerNameService.resolveGramsevakDisplayName(tenantId).orElse(null);
        return TenantProfileDto.fromParts(tenant, sarpanch, gramsevak);
    }

    private static String normalizeLang(String langRaw) {
        if (langRaw == null || langRaw.isBlank()) {
            return "mr";
        }
        String l = langRaw.trim().toLowerCase();
        if ("en".equals(l) || "mr".equals(l)) {
            return l;
        }
        return "mr";
    }
}
