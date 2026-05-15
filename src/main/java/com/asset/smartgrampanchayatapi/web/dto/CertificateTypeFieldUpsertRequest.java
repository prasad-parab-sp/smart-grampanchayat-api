package com.asset.smartgrampanchayatapi.web.dto;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "CertificateTypeFieldUpsertRequest", description = "Dynamic apply-form field for a new certificate type")
public record CertificateTypeFieldUpsertRequest(
        @NotBlank
        @Size(max = 120)
        @Pattern(regexp = "[a-z][a-z0-9_]*", message = "fieldKey must start with a letter and use lowercase snake_case")
        @Schema(example = "plot_survey_no")
        String fieldKey,
        @NotBlank
        @Size(max = 500)
        String labelMr,
        @Size(max = 500)
        String labelEn,
        @Size(max = 500)
        String placeholderMr,
        @Size(max = 500)
        String placeholderEn,
        String helpTextMr,
        String helpTextEn,
        @NotBlank
        @Pattern(regexp = "TEXT|TEXTAREA|DATE|NUMBER|SELECT|FILE")
        String dataType,
        boolean required,
        int sortOrder,
        @Schema(description = "For SELECT: JSON array of {value, label_mr, label_en}") JsonNode optionsJson,
        Integer maxFiles,
        Long maxBytes
) {
}
