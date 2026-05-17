package com.asset.smartgrampanchayatapi.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.asset.smartgrampanchayatapi.district.jpa.model.CitizenStatus;
import com.asset.smartgrampanchayatapi.district.jpa.model.GenderType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(name = "CitizenUpsertRequest")
public record CitizenUpsertRequest(
        @NotBlank @Size(max = 100) String firstName,
        @Size(max = 100) String middleName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull LocalDate dateOfBirth,
        @NotNull GenderType gender,
        @Size(max = 255) String addressLine1,
        @Size(max = 10) String wardNumber,
        @Size(max = 15) String mobile,
        @Size(max = 20) String voterId,
        @Size(max = 30) String rationCardNumber,
        boolean bpl,
        @Size(max = 30) String bplCardNumber,
        BigDecimal annualIncome,
        boolean disabled,
        @Size(max = 100) String disabilityType,
        @NotNull CitizenStatus status
) {
}
