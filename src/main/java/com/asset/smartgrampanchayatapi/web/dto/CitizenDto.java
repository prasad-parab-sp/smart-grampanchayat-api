package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import com.asset.smartgrampanchayatapi.district.jpa.model.Citizen;
import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenStatus;
import com.asset.smartgrampanchayatapi.district.jpa.model.GenderType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CitizenDto")
public record CitizenDto(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String citizenUid,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String firstName,
        String middleName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String lastName,
        LocalDate dateOfBirth,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) GenderType gender,
        String addressLine1,
        String wardNumber,
        String mobile,
        String voterId,
        String rationCardNumber,
        boolean bpl,
        String bplCardNumber,
        BigDecimal annualIncome,
        boolean disabled,
        String disabilityType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) CitizenStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant updatedAt
) {
    public static CitizenDto fromEntity(Citizen row) {
        return new CitizenDto(
                row.getId(),
                row.getCitizenUid(),
                row.getFirstName(),
                row.getMiddleName(),
                row.getLastName(),
                row.getDateOfBirth(),
                row.getGender(),
                row.getAddressLine1(),
                row.getWardNumber(),
                row.getMobile(),
                row.getVoterId(),
                row.getRationCardNumber(),
                row.isBpl(),
                row.getBplCardNumber(),
                row.getAnnualIncome(),
                row.isDisabled(),
                row.getDisabilityType(),
                row.getStatus(),
                row.getCreatedAt(),
                row.getUpdatedAt()
        );
    }

    public String displayName() {
        StringBuilder sb = new StringBuilder();
        if (rowPart(firstName)) {
            sb.append(firstName.trim());
        }
        if (rowPart(middleName)) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(middleName.trim());
        }
        if (rowPart(lastName)) {
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(lastName.trim());
        }
        return sb.toString();
    }

    private static boolean rowPart(String value) {
        return value != null && !value.isBlank();
    }
}
