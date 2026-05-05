package com.asset.smartgrampanchayatapi.web.dto;

import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateTypeField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;

/** One dynamic form row from {@code certificate_type_field}; values go in {@code certificate_application.additional_values_json}. */
@Schema(name = "CertificateTypeFieldDto", description = "Per-certificate-type form field metadata")
public record CertificateTypeFieldDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String fieldKey,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String labelMr,
        @Schema(description = "English label; UI may fall back to labelMr") String labelEn,
        String placeholderMr,
        String placeholderEn,
        String helpTextMr,
        String helpTextEn,
        @Schema(description = "TEXT, TEXTAREA, DATE, NUMBER, SELECT, or FILE", requiredMode = Schema.RequiredMode.REQUIRED) String dataType,
        @JsonProperty("required")
        @JsonInclude(JsonInclude.Include.ALWAYS)
        @Schema(
                description = "Whether the applicant must submit this field (maps to certificate_type_field.required)",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean required,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int sortOrder,
        @Schema(description = "For SELECT: [{\"value\":\"X\",\"label_mr\":\"…\",\"label_en\":\"…\"}]") JsonNode optionsJson,
        Integer maxFiles,
        Long maxBytes,
        String allowedMimeCsv
) {
    public static CertificateTypeFieldDto fromEntity(CertificateTypeField e) {
        return new CertificateTypeFieldDto(
                e.getId(),
                e.getFieldKey(),
                e.getLabelMr(),
                e.getLabelEn(),
                e.getPlaceholderMr(),
                e.getPlaceholderEn(),
                e.getHelpTextMr(),
                e.getHelpTextEn(),
                e.getDataType(),
                e.isRequired(),
                e.getSortOrder(),
                e.getOptionsJson(),
                e.getMaxFiles() != null ? e.getMaxFiles().intValue() : null,
                e.getMaxBytes(),
                e.getAllowedMimeCsv()
        );
    }
}
