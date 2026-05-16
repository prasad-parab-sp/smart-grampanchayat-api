package com.asset.smartgrampanchayatapi.web.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.GpNotice;
import com.asset.smartgrampanchayatapi.district.jpa.model.NoticeType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "NoticeDto")
public record NoticeDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) NoticeType noticeType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String title,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String body,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate publishedOn,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate expiresOn,
        boolean sendToCitizens,
        boolean sendToMembers,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {
    public static NoticeDto fromEntity(GpNotice row) {
        return new NoticeDto(
                row.getId(),
                row.getNoticeType(),
                row.getTitle(),
                row.getBody(),
                row.getPublishedOn(),
                row.getExpiresOn(),
                row.isSendToCitizens(),
                row.isSendToMembers(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }
}
