package com.asset.smartgrampanchayatapi.district.service.notice;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.GpNotice;
import com.asset.smartgrampanchayatapi.district.jpa.model.NoticeType;
import com.asset.smartgrampanchayatapi.district.jpa.repository.GpNoticeRepository;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingContext;
import com.asset.smartgrampanchayatapi.web.dto.NoticeDto;
import com.asset.smartgrampanchayatapi.web.dto.NoticeUpsertRequest;

@Service
public class NoticeDataAccessService {

    private final GpNoticeRepository gpNoticeRepository;

    public NoticeDataAccessService(GpNoticeRepository gpNoticeRepository) {
        this.gpNoticeRepository = gpNoticeRepository;
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public List<NoticeDto> listNotices(UUID tenantId, NoticeType noticeType, boolean includeExpired) {
        LocalDate today = LocalDate.now();
        List<GpNotice> rows = noticeType == null
                ? gpNoticeRepository.findByTenantForList(tenantId, today, includeExpired)
                : gpNoticeRepository.findByTenantAndTypeForList(tenantId, noticeType, today, includeExpired);
        return rows.stream().map(NoticeDto::fromEntity).toList();
    }

    @Transactional(transactionManager = "districtTransactionManager", readOnly = true)
    public Optional<NoticeDto> findNotice(UUID tenantId, UUID id, boolean includeExpired) {
        return gpNoticeRepository
                .findByTenantIdAndId(tenantId, id)
                .filter(row -> includeExpired || !isExpired(row, LocalDate.now()))
                .map(NoticeDto::fromEntity);
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public NoticeDto insertNotice(TenantShardRoutingContext ctx, NoticeUpsertRequest body) {
        Instant now = Instant.now();
        GpNotice row = new GpNotice();
        row.setId(UUID.randomUUID());
        row.setTenantId(ctx.tenantId());
        applyUpsert(row, body);
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        return NoticeDto.fromEntity(gpNoticeRepository.save(row));
    }

    @Transactional(transactionManager = "districtTransactionManager")
    public void deleteNotice(UUID tenantId, UUID id) {
        GpNotice row = gpNoticeRepository
                .findByTenantIdAndId(tenantId, id)
                .orElseThrow();
        gpNoticeRepository.delete(row);
    }

    private static void applyUpsert(GpNotice row, NoticeUpsertRequest body) {
        if (body.expiresOn().isBefore(body.publishedOn())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Expiry date cannot be before publish date."
            );
        }
        row.setNoticeType(body.noticeType());
        row.setTitle(body.title().trim());
        row.setBody(body.body().trim());
        row.setPublishedOn(body.publishedOn());
        row.setExpiresOn(body.expiresOn());
        row.setSendToCitizens(body.sendToCitizens() == null || body.sendToCitizens());
        row.setSendToMembers(body.sendToMembers() != null && body.sendToMembers());
    }

    private static boolean isExpired(GpNotice row, LocalDate today) {
        return row.getExpiresOn().isBefore(today);
    }
}
