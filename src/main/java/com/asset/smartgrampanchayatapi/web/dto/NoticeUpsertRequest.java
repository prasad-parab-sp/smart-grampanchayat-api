package com.asset.smartgrampanchayatapi.web.dto;

import java.time.LocalDate;

import com.asset.smartgrampanchayatapi.district.jpa.model.NoticeType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "NoticeUpsertRequest")
public record NoticeUpsertRequest(
        @NotNull NoticeType noticeType,
        @NotBlank @Size(max = 500) String title,
        @NotBlank String body,
        @NotNull LocalDate publishedOn,
        @NotNull LocalDate expiresOn,
        Boolean sendToCitizens,
        Boolean sendToMembers
) {
}
