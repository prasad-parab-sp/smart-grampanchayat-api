package com.asset.smartgrampanchayatapi.district.service.notice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.asset.smartgrampanchayatapi.district.jpa.model.NoticeType;
import com.asset.smartgrampanchayatapi.district.routing.TenantCodeContext;
import com.asset.smartgrampanchayatapi.district.service.routing.TenantShardRoutingService;
import com.asset.smartgrampanchayatapi.district.service.user.UserService;
import com.asset.smartgrampanchayatapi.web.dto.NoticeCreateRequest;
import com.asset.smartgrampanchayatapi.web.dto.NoticeDeleteRequest;
import com.asset.smartgrampanchayatapi.web.dto.NoticeDto;

@Service
public class NoticeService {

    private final TenantShardRoutingService tenantShardRoutingService;
    private final NoticeDataAccessService noticeDataAccessService;
    private final UserService userService;

    public NoticeService(
            TenantShardRoutingService tenantShardRoutingService,
            NoticeDataAccessService noticeDataAccessService,
            UserService userService
    ) {
        this.tenantShardRoutingService = tenantShardRoutingService;
        this.noticeDataAccessService = noticeDataAccessService;
        this.userService = userService;
    }

    public Optional<List<NoticeDto>> listNotices(NoticeType noticeType, boolean includeExpired) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load notices from district database",
                ctx -> Optional.of(
                        noticeDataAccessService.listNotices(ctx.tenantId(), noticeType, includeExpired)
                )
        );
    }

    public Optional<NoticeDto> getNotice(UUID id, boolean includeExpired) {
        return tenantShardRoutingService.runOnShard(
                TenantCodeContext.getRequired(),
                "Could not load notice from district database",
                ctx -> noticeDataAccessService.findNotice(ctx.tenantId(), id, includeExpired)
        );
    }

    public NoticeDto createNotice(NoticeCreateRequest request) {
        userService.verifyActiveStaffForNoticeWrite(request.staffUserId());
        return tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not create notice on district database",
                        ctx -> Optional.of(
                                noticeDataAccessService.insertNotice(ctx, request.notice())
                        )
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }

    public void deleteNotice(UUID id, NoticeDeleteRequest request) {
        userService.verifyActiveStaffForNoticeWrite(request.staffUserId());
        tenantShardRoutingService
                .runOnShard(
                        TenantCodeContext.getRequired(),
                        "Could not delete notice on district database",
                        ctx -> {
                            if (noticeDataAccessService.findNotice(ctx.tenantId(), id, true).isEmpty()) {
                                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Notice not found.");
                            }
                            noticeDataAccessService.deleteNotice(ctx.tenantId(), id);
                            return Optional.of(Boolean.TRUE);
                        }
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Unknown tenant code."
                ));
    }
}
