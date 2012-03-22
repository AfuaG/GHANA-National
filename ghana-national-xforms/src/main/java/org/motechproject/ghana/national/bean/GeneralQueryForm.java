package org.motechproject.ghana.national.bean;

import org.motechproject.mobileforms.api.domain.FormBean;
import org.motechproject.mobileforms.api.validator.annotations.Required;

public class GeneralQueryForm extends FormBean {
    @Required
    private String staffId;
    @Required
    private String facilityId;
    @Required
    private String responsePhoneNumber;
    @Required
    private String queryType;

    public String getStaffId() {
        return this.staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public String getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getResponsePhoneNumber() {
        return responsePhoneNumber;
    }

    public void setResponsePhoneNumber(String responsePhoneNumber) {
        this.responsePhoneNumber = responsePhoneNumber;
    }

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }
}
