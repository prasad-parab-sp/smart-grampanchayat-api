package com.asset.smartgrampanchayatapi.web.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "NoticeDeleteRequest")
public record NoticeDeleteRequest(@NotNull UUID staffUserId) {
}
