package org.motechproject.ghana.national.vo;

import org.motechproject.ghana.national.domain.ANCCareHistory;
import org.motechproject.ghana.national.domain.RegistrationToday;

import java.util.Date;
import java.util.List;

public class ANCVO {

    private String staffId;
    private String facilityId;
    private String motechPatientId;
    private Date registrationDate;
    private RegistrationToday registrationToday;
    private String serialNumber;
    private Date estimatedDateOfDelivery;
    private Double height;
    private Integer gravida;
    private Integer parity;
    private Boolean addHistory;
    private Boolean deliveryDateConfirmed;
    private ANCCareHistoryVO ancCareHistoryVO;

    public ANCVO(String staffId, String facilityId, String motechPatientId, Date registrationDate, RegistrationToday registrationToday, String serialNumber,
                 Date estimatedDateOfDelivery, Double height, Integer gravida, Integer parity, Boolean addHistory, Boolean deliveryDateConfirmed,
                 List<ANCCareHistory> careHistory, String lastIPT, String lastTT,String lastHbLevels, String lastMotherVitaminA,
                 String lastIronOrFolate, String lastSyphilis, String lastMalaria, String lastDiarrhea,String lastPnuemonia, Date lastIPTDate, Date lastTTDate,
                 Date lastHbDate, Date lastMotherVitaminADate, Date lastIronOrFolateDate, Date lastSyphilisDate, Date lastMalariaDate, Date lastDiarrheaDate, Date lastPnuemoniaDate, Boolean addCareHistory) {
        this.staffId = staffId;
        this.facilityId = facilityId;
        this.motechPatientId = motechPatientId;
        this.registrationDate = registrationDate;
        this.registrationToday = registrationToday;
        this.serialNumber = serialNumber;
        this.estimatedDateOfDelivery = estimatedDateOfDelivery;
        this.height = height;
        this.gravida = gravida;
        this.parity = parity;
        this.addHistory = addHistory;
        this.deliveryDateConfirmed = deliveryDateConfirmed;
        this.ancCareHistoryVO = new ANCCareHistoryVO(addCareHistory, careHistory, lastIPT, lastTT, lastHbLevels, lastMotherVitaminA, lastIronOrFolate, lastSyphilis, lastMalaria, lastDiarrhea,
                lastPnuemonia, lastIPTDate, lastTTDate,lastHbDate, lastMotherVitaminADate, lastIronOrFolateDate, lastSyphilisDate, lastMalariaDate, lastDiarrheaDate, lastPnuemoniaDate );
    }

    public String getStaffId() {
        return staffId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public String getPatientMotechId() {
        return motechPatientId;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public RegistrationToday getRegistrationToday() {
        return registrationToday;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public Date getEstimatedDateOfDelivery() {
        return estimatedDateOfDelivery;
    }

    public Double getHeight() {
        return height;
    }

    public Integer getGravida() {
        return gravida;
    }

    public Integer getParity() {
        return parity;
    }

    public Boolean getAddHistory() {
        return addHistory;
    }

    public Boolean getDeliveryDateConfirmed() {
        return deliveryDateConfirmed;
    }

    public ANCCareHistoryVO getAncCareHistoryVO() {
        return ancCareHistoryVO;
    }
}
