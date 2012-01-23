package org.motechproject.ghana.national.vo;

import org.motechproject.ghana.national.domain.ANCCareHistory;

import java.util.Date;
import java.util.List;

public class ANCCareHistoryVO {

    Boolean addCareHistory;
    List<ANCCareHistory> careHistory;
    String lastIPT;
    String lastTT;
    Date lastTTDate;
    Date lastIPTDate;


    public ANCCareHistoryVO(Boolean addCareHistory, List<ANCCareHistory> careHistory, String lastIPT, String lastTT, Date lastIPTDate, Date lastTTDate) {
        this.addCareHistory = addCareHistory;
        this.careHistory=careHistory;
        this.lastIPT=lastIPT;
        this.lastTT=lastTT;
        this.lastIPTDate=lastIPTDate;
        this.lastTTDate=lastTTDate;
    }

    public List<ANCCareHistory> getCareHistory() {
        return careHistory;
    }

    public String getLastIPT() {
        return lastIPT;
    }

    public String getLastTT() {
        return lastTT;
    }

    public Date getLastIPTDate() {
        return lastIPTDate;
    }

    public Date getLastTTDate() {
        return lastTTDate;
    }

    public Boolean getAddCareHistory() {
        return addCareHistory;
    }
}