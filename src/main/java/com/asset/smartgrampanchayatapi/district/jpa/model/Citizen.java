package com.asset.smartgrampanchayatapi.district.jpa.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * District shard {@code citizens} row — resident register for a GP (no login; separate from {@linkplain ShardUser}).
 */
@Entity
@Table(
        name = "citizens",
        uniqueConstraints = @UniqueConstraint(name = "uq_citizens_tenant_mobile", columnNames = {"tenant_id", "mobile"})
)
public class Citizen {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "grampanchayat_id", nullable = false)
    private UUID grampanchayatId;

    @Column(name = "citizen_uid", nullable = false, unique = true, length = 25)
    private String citizenUid;

    @Column(name = "aadhaar_number", length = 255)
    private String aadhaarNumber;

    @Column(name = "voter_id", length = 20)
    private String voterId;

    @Column(name = "ration_card_number", length = 30)
    private String rationCardNumber;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "middle_name", length = 100)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "gender_type")
    private GenderType gender;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "marital_status", columnDefinition = "marital_status")
    private MaritalStatus maritalStatus;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(length = 50)
    private String religion;

    @Column(length = 100)
    private String caste;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "caste_category", columnDefinition = "caste_category")
    private CasteCategory casteCategory;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "ward_number", length = 10)
    private String wardNumber;

    @Column(length = 15)
    private String mobile;

    @Column(name = "alternate_mobile", length = 15)
    private String alternateMobile;

    @Column(length = 255)
    private String email;

    @Column(length = 100)
    private String occupation;

    @Column(name = "annual_income", precision = 12, scale = 2)
    private BigDecimal annualIncome;

    @Column(name = "is_bpl", nullable = false)
    private boolean bpl = false;

    @Column(name = "bpl_card_number", length = 30)
    private String bplCardNumber;

    @Column(name = "is_disabled", nullable = false)
    private boolean disabled = false;

    @Column(name = "disability_type", length = 100)
    private String disabilityType;

    @Column(name = "is_senior_citizen", nullable = false)
    private boolean seniorCitizen = false;

    @Column(name = "photo_url", columnDefinition = "text")
    private String photoUrl;

    @Column(name = "head_of_family_id")
    private UUID headOfFamilyId;

    @Column(name = "is_head_of_family", nullable = false)
    private boolean headOfFamily = false;

    @Column(name = "family_id", length = 20)
    private String familyId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "citizen_status")
    private CitizenStatus status = CitizenStatus.active;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    protected Citizen() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public void setTenantId(UUID tenantId) {
        this.tenantId = tenantId;
    }

    public UUID getGrampanchayatId() {
        return grampanchayatId;
    }

    public void setGrampanchayatId(UUID grampanchayatId) {
        this.grampanchayatId = grampanchayatId;
    }

    public String getCitizenUid() {
        return citizenUid;
    }

    public void setCitizenUid(String citizenUid) {
        this.citizenUid = citizenUid;
    }

    public String getAadhaarNumber() {
        return aadhaarNumber;
    }

    public void setAadhaarNumber(String aadhaarNumber) {
        this.aadhaarNumber = aadhaarNumber;
    }

    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getRationCardNumber() {
        return rationCardNumber;
    }

    public void setRationCardNumber(String rationCardNumber) {
        this.rationCardNumber = rationCardNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public GenderType getGender() {
        return gender;
    }

    public void setGender(GenderType gender) {
        this.gender = gender;
    }

    public MaritalStatus getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(MaritalStatus maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getCaste() {
        return caste;
    }

    public void setCaste(String caste) {
        this.caste = caste;
    }

    public CasteCategory getCasteCategory() {
        return casteCategory;
    }

    public void setCasteCategory(CasteCategory casteCategory) {
        this.casteCategory = casteCategory;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAlternateMobile() {
        return alternateMobile;
    }

    public void setAlternateMobile(String alternateMobile) {
        this.alternateMobile = alternateMobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }

    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }

    public boolean isBpl() {
        return bpl;
    }

    public void setBpl(boolean bpl) {
        this.bpl = bpl;
    }

    public String getBplCardNumber() {
        return bplCardNumber;
    }

    public void setBplCardNumber(String bplCardNumber) {
        this.bplCardNumber = bplCardNumber;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getDisabilityType() {
        return disabilityType;
    }

    public void setDisabilityType(String disabilityType) {
        this.disabilityType = disabilityType;
    }

    public boolean isSeniorCitizen() {
        return seniorCitizen;
    }

    public void setSeniorCitizen(boolean seniorCitizen) {
        this.seniorCitizen = seniorCitizen;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public UUID getHeadOfFamilyId() {
        return headOfFamilyId;
    }

    public void setHeadOfFamilyId(UUID headOfFamilyId) {
        this.headOfFamilyId = headOfFamilyId;
    }

    public boolean isHeadOfFamily() {
        return headOfFamily;
    }

    public void setHeadOfFamily(boolean headOfFamily) {
        this.headOfFamily = headOfFamily;
    }

    public String getFamilyId() {
        return familyId;
    }

    public void setFamilyId(String familyId) {
        this.familyId = familyId;
    }

    public CitizenStatus getStatus() {
        return status;
    }

    public void setStatus(CitizenStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }
}
