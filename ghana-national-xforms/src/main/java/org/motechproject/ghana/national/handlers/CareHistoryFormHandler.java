package org.motechproject.ghana.national.handlers;

import org.motechproject.ghana.national.bean.CareHistoryForm;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.exception.XFormHandlerException;
import org.motechproject.ghana.national.service.CareService;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
import org.motechproject.model.MotechEvent;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.server.event.annotations.MotechListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class CareHistoryFormHandler implements FormPublishHandler {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private CareService careService;

    @Autowired
    private FacilityService facilityService;

    @Override
    @MotechListener(subjects = "form.validation.successful.NurseDataEntry.careHistory")
    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(MotechEvent event) {
        try {
            CareHistoryForm careHistoryForm = (CareHistoryForm) event.getParameters().get(Constants.FORM_BEAN);
            String facilityId = facilityService.getFacilityByMotechId(careHistoryForm.getFacilityId()).getMrsFacilityId();
            careService.addCareHistory(careHistoryForm.careHistoryVO(facilityId));
        } catch (Exception e) {
            log.error("Encountered error while saving care history details", e);
            throw new XFormHandlerException(event.getSubject(), e);
        }
    }
}
