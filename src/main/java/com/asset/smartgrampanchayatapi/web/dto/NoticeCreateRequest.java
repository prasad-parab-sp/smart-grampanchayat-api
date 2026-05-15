package com.asset.smartgrampanchayatapi.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(name = "NoticeCreateRequest")
public record NoticeCreateRequest(
        @NotNull UUID staffUserId,
        @NotNull @Valid NoticeUpsertRequest notice
) {
}
