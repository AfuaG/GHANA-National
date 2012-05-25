package org.motechproject.ghana.national.bean;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ghana.national.domain.ANCCareHistory;
import org.motechproject.ghana.national.domain.CwcCareHistory;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.domain.RegistrationType;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.validator.field.MotechId;
import org.motechproject.mobileforms.api.validator.annotations.MaxLength;
import org.motechproject.mobileforms.api.validator.annotations.RegEx;
import org.motechproject.mobileforms.api.validator.annotations.Required;
import org.motechproject.openmrs.omod.validator.MotechIdVerhoeffValidator;
import org.motechproject.openmrs.omod.validator.VerhoeffValidator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.motechproject.ghana.national.FormFieldRegExPatterns.*;
import static org.motechproject.util.DateUtil.newDateTime;

public class RegisterClientForm extends MobileMidWifeIncludeForm {

    @Required
    private RegistrationType registrationMode;
    @RegEx(pattern = MOTECH_ID_PATTERN) @MotechId(validator = MotechIdVerhoeffValidator.class)
    private String motechId;
    @RegEx(pattern = MOTECH_ID_PATTERN) @MotechId(validator = MotechIdVerhoeffValidator.class)
    private String motherMotechId;
    @Required
    private PatientType registrantType;
    @Required @MaxLength(size = 50) @RegEx(pattern = NAME_PATTERN)
    private String firstName;
    @MaxLength(size = 50) @RegEx(pattern = NAME_PATTERN)
    private String middleName;
    @Required @MaxLength(size = 50) @RegEx(pattern = NAME_PATTERN)
    private String lastName;
    @Required
    private Date dateOfBirth;
    @Required
    private Boolean estimatedBirthDate;
    @RegEx(pattern = GENDER_PATTERN)
    private String sex;
    @Required
    private Boolean insured;
    @MaxLength(size = 30) @RegEx(pattern = NHIS_NO_PATTERN)
    private String nhis;
    private Date nhisExpires;
    private String region;
    private String district;
    private String subDistrict;
    @Required @MaxLength(size = 50) @RegEx(pattern = NUMERIC_OR_NOTAPPLICABLE_PATTERN)
    private String facilityId;
    @Required @MaxLength(size = 50)
    private String address;
    private String sender;
    @Required @MaxLength(size = 50) @RegEx(pattern = NUMERIC_OR_NOTAPPLICABLE_PATTERN) @MotechId(validator = VerhoeffValidator.class)
    private String staffId;
    @Required
    private Date date;
    @RegEx(pattern = PHONE_NO_PATTERN)
    private String phoneNumber;

    @RegEx(pattern = PHONE_NO_PATTERN)
    private String mmRegPhone;

    //CWC REGISTRATION FIELDS
    private String cwcRegNumber;

    private Boolean addHistory;

    private String addChildHistory;
    private String addMotherHistory;

    private Date bcgDate;
    private Date lastOPVDate;
    private Date lastVitaminADate;
    private Date lastIPTiDate;
    private Date yellowFeverDate;
    private Date lastPentaDate;
    private Date measlesDate;
    private Integer lastOPV;
    private Integer lastIPTi;
    private Integer lastPenta;

    //ANC REGISTRATION DETAILS

    private String ancRegNumber;
    private Date expDeliveryDate;
    private Boolean deliveryDateConfirmed;
    private Double height;
    private Integer gravida;
    private Integer parity;
    private Date lastIPTDate;
    private Date lastTTDate;
    private String lastIPT;
    private String lastTT;

    public String getMmRegPhone() {
        return mmRegPhone;
    }

    public void setMmRegPhone(String mmRegPhone) {
        this.mmRegPhone = mmRegPhone;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMotechId() {
        return motechId;
    }

    public String getMotherMotechId() {
        return motherMotechId;
    }

    public PatientType getRegistrantType() {
        return registrantType;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public Boolean getEstimatedBirthDate() {
        return estimatedBirthDate;
    }

    public String getSex() {
        return sex;
    }

    public Boolean getInsured() {
        return insured;
    }

    public String getNhis() {
        return nhis;
    }

    public Date getNhisExpires() {
        return nhisExpires;
    }

    public String getRegion() {
        return region;
    }

    public String getDistrict() {
        return district;
    }

    public String getSubDistrict() {
        return subDistrict;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getAddress() {
        return address;
    }

    public String getSender() {
        return sender;
    }

    public void setMotechId(String motechId) {
        this.motechId = motechId;
    }

    public void setMotherMotechId(String motherMotechId) {
        this.motherMotechId = motherMotechId;
    }

    public void setRegistrantType(PatientType registrantType) {
        this.registrantType = registrantType;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setEstimatedBirthDate(Boolean estimatedBirthDate) {
        this.estimatedBirthDate = estimatedBirthDate;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setInsured(Boolean insured) {
        this.insured = insured;
    }

    public void setNhis(String nhis) {
        this.nhis = nhis;
    }

    public void setNhisExpires(Date nhisExpires) {
        this.nhisExpires = nhisExpires;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setSubDistrict(String subDistrict) {
        this.subDistrict = subDistrict;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public RegistrationType getRegistrationMode() {
        return registrationMode;
    }

    public void setRegistrationMode(RegistrationType registrationMode) {
        this.registrationMode = registrationMode;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCwcRegNumber() {
        return cwcRegNumber;
    }

    public void setCwcRegNumber(String cwcRegNumber) {
        this.cwcRegNumber = cwcRegNumber;
    }

    public Boolean getAddHistory() {
        return addHistory;
    }

    public void setAddHistory(Boolean addHistory) {
        this.addHistory = addHistory;
    }

    public Date getBcgDate() {
        return bcgDate;
    }

    public void setBcgDate(Date bcgDate) {
        this.bcgDate = bcgDate;
    }

    public Date getLastOPVDate() {
        return lastOPVDate;
    }

    public void setLastOPVDate(Date lastOPVDate) {
        this.lastOPVDate = lastOPVDate;
    }

    public Date getLastVitaminADate() {
        return lastVitaminADate;
    }

    public void setLastVitaminADate(Date lastVitaminADate) {
        this.lastVitaminADate = lastVitaminADate;
    }

    public Date getLastIPTiDate() {
        return lastIPTiDate;
    }

    public void setLastIPTiDate(Date lastIPTiDate) {
        this.lastIPTiDate = lastIPTiDate;
    }

    public Date getYellowFeverDate() {
        return yellowFeverDate;
    }

    public void setYellowFeverDate(Date yellowFeverDate) {
        this.yellowFeverDate = yellowFeverDate;
    }

    public Date getLastPentaDate() {
        return lastPentaDate;
    }

    public void setLastPentaDate(Date lastPentaDate) {
        this.lastPentaDate = lastPentaDate;
    }

    public Date getMeaslesDate() {
        return measlesDate;
    }

    public void setMeaslesDate(Date measlesDate) {
        this.measlesDate = measlesDate;
    }

    public Integer getLastOPV() {
        return lastOPV;
    }

    public void setLastOPV(Integer lastOPV) {
        this.lastOPV = lastOPV;
    }

    public Integer getLastIPTi() {
        return lastIPTi;
    }

    public void setLastIPTi(Integer lastIPTi) {
        this.lastIPTi = lastIPTi;
    }

    public Integer getLastPenta() {
        return lastPenta;
    }

    public void setLastPenta(Integer lastPenta) {
        this.lastPenta = lastPenta;
    }

    public String getAddMotherHistory() {
        return addMotherHistory;
    }

    public void setAddMotherHistory(String addMotherHistory) {
        this.addMotherHistory = addMotherHistory;
    }

    public String getAddChildHistory() {
        return addChildHistory;
    }

    public void setAddChildHistory(String addChildHistory) {
        this.addChildHistory = addChildHistory;
    }

    public String getAncRegNumber() {
        return ancRegNumber;
    }

    public void setAncRegNumber(String ancRegNumber) {
        this.ancRegNumber = ancRegNumber;
    }

    public Date getExpDeliveryDate() {
        return expDeliveryDate;
    }

    public void setExpDeliveryDate(Date expDeliveryDate) {
        this.expDeliveryDate = expDeliveryDate;
    }

    public Boolean getDeliveryDateConfirmed() {
        return deliveryDateConfirmed;
    }

    public void setDeliveryDateConfirmed(Boolean deliveryDateConfirmed) {
        this.deliveryDateConfirmed = deliveryDateConfirmed;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Integer getGravida() {
        return gravida;
    }

    public void setGravida(Integer gravida) {
        this.gravida = gravida;
    }

    public Integer getParity() {
        return parity;
    }

    public void setParity(Integer parity) {
        this.parity = parity;
    }

    public Date getLastIPTDate() {
        return lastIPTDate;
    }

    public void setLastIPTDate(Date lastIPTDate) {
        this.lastIPTDate = lastIPTDate;
    }

    public Date getLastTTDate() {
        return lastTTDate;
    }

    public void setLastTTDate(Date lastTTDate) {
        this.lastTTDate = lastTTDate;
    }

    public String getLastIPT() {
        return lastIPT;
    }

    public void setLastIPT(String lastIPT) {
        this.lastIPT = lastIPT;
    }

    public String getLastTT() {
        return lastTT;
    }

    public void setLastTT(String lastTT) {
        this.lastTT = lastTT;
    }

    public List<ANCCareHistory> getAncCareHistories() {
        List<ANCCareHistory> ancCareHistories = new ArrayList<ANCCareHistory>();
        if (StringUtils.isNotEmpty(addMotherHistory)) {
            for (String value : addMotherHistory.split(" ")) {
                ancCareHistories.add(ANCCareHistory.valueOf(value));
            }
        }
        return ancCareHistories;
    }

    public List<CwcCareHistory> getCWCCareHistories() {
        List<CwcCareHistory> cwcCareHistories = new ArrayList<CwcCareHistory>();
        if (StringUtils.isNotEmpty(addChildHistory)) {
            for (String value : addChildHistory.split(" ")) {
                cwcCareHistories.add(CwcCareHistory.valueOf(value));
            }
        }
        return cwcCareHistories;
    }

    public MobileMidwifeEnrollment createMobileMidwifeEnrollment(String patientMotechId) {
        return fillEnrollment(new MobileMidwifeEnrollment(newDateTime(getDate()))).setStaffId(getStaffId()).setPatientId(patientMotechId);
    }

    @Override
    public String groupId() {
        return motechId;
    }
}
