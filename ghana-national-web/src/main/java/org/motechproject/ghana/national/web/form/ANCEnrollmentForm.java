package org.motechproject.ghana.national.web.form;

import org.motechproject.ghana.national.domain.ANCCareHistory;
import org.motechproject.ghana.national.domain.RegistrationToday;

import java.util.Date;
import java.util.List;

public class ANCEnrollmentForm {
    private String motechPatientId;
    private String serialNumber;
    private RegistrationToday registrationToday;
    private Date registrationDate;
    private Float height;
    private Integer gravida;
    private Integer parity;
    private Date estimatedDateOfDelivery;
    private FacilityForm facilityForm;
    private Boolean addHistory;
    private List<ANCCareHistory> careHistory;
    private String lastIPT;
    private String lastTT;
    private Date lastIPTDate;
    private Date lastTTDate;

    public ANCEnrollmentForm() {
    }

    public ANCEnrollmentForm(String motechPatientId, String serialNumber, Date registrationDate) {
        this.motechPatientId = motechPatientId;
        this.serialNumber = serialNumber;
        this.registrationDate = registrationDate;
    }

    public ANCEnrollmentForm(String motechPatientId) {
        this.motechPatientId = motechPatientId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public FacilityForm getFacilityForm() {
        return facilityForm;
    }

    public void setFacilityForm(FacilityForm facilityForm) {
        this.facilityForm = facilityForm;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
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

    public Boolean getAddHistory() {
        return addHistory;
    }

    public void setAddHistory(Boolean addHistory) {
        this.addHistory = addHistory;
    }

    public String getLastTT() {
        return lastTT;
    }

    public void setLastTT(String lastTT) {
        this.lastTT = lastTT;
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

    public RegistrationToday getRegistrationToday() {
        return registrationToday;
    }

    public void setRegistrationToday(RegistrationToday registrationToday) {
        this.registrationToday = registrationToday;
    }

    public String getMotechPatientId() {
        return motechPatientId;
    }

    public void setMotechPatientId(String motechPatientId) {
        this.motechPatientId = motechPatientId;
    }

    public Date getEstimatedDateOfDelivery() {
        return estimatedDateOfDelivery;
    }

    public void setEstimatedDateOfDelivery(Date estimatedDateOfDelivery) {
        this.estimatedDateOfDelivery = estimatedDateOfDelivery;
    }

    public List<ANCCareHistory> getCareHistory() {
        return careHistory;
    }

    public void setCareHistory(List<ANCCareHistory> careHistory) {
        this.careHistory = careHistory;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }
}
