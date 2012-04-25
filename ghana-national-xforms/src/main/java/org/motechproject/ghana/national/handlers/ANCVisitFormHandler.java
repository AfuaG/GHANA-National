package org.motechproject.ghana.national.handlers;

import org.motechproject.ghana.national.bean.ANCVisitForm;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.exception.XFormHandlerException;
import org.motechproject.ghana.national.repository.AllSchedules;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.MotherVisitService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.ghana.national.service.StaffService;
import org.motechproject.ghana.national.service.request.ANCVisitRequest;
import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.MRSUser;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.server.event.annotations.MotechListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.ghana.national.domain.Constants.FORM_BEAN;

@Component
public class ANCVisitFormHandler implements FormPublishHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    MotherVisitService visitService;

    @Autowired
    FacilityService facilityService;
    @Autowired
    private StaffService staffService;
    @Autowired
    private PatientService patientService;
    @Autowired
    private AllSchedules allSchedules;

    @Override
    @MotechListener(subjects = "form.validation.successful.NurseDataEntry.ancVisit")
    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(MotechEvent event) {
        try {
            ANCVisitForm form = (ANCVisitForm) event.getParameters().get(FORM_BEAN);
            ANCVisitRequest ancVisit = createANCVisit(form);
            visitService.registerANCVisit(ancVisit);
        } catch (Exception e) {
            log.error("Encountered error while saving ANC visit details", e);
            throw new XFormHandlerException(event.getSubject(), e);
        }
    }

    private ANCVisitRequest createANCVisit(ANCVisitForm form) {
        Facility facility = facilityService.getFacilityByMotechId(form.getFacilityId());
        MRSUser staff = staffService.getUserByEmailIdOrMotechId(form.getStaffId());
        Patient patient = patientService.getPatientByMotechId(form.getMotechId());

        return new ANCVisitRequest().staff(staff).facility(facility).patient(patient)
                .date(form.getDate()).serialNumber(form.getSerialNumber()).visitNumber(form.getVisitNumber())
                .estDeliveryDate(form.getEstDeliveryDate()).bpDiastolic(form.getBpDiastolic()).bpSystolic(form.getBpSystolic()).pmtct(form.getPmtct())
                .weight(form.getWeight()).ttdose(form.getTtdose()).iptdose(form.getIptdose()).hemoglobin(form.getHemoglobin()).dewormer(form.getDewormer())
                .iptReactive(form.getIptReactive()).itnUse(form.getItnUse()).fhr(form.getFhr()).vdrlReactive(form.getVdrlReactive()).vdrlTreatment(form.getVdrlTreatment())
                .fht(form.getFht()).urineTestProteinPositive(form.getUrineTestProteinPositive()).urineTestGlucosePositive(form.getUrineTestGlucosePositive())
                .preTestCounseled(form.getPreTestCounseled()).hivTestResult(form.getHivTestResult()).postTestCounseled(form.getPostTestCounseled())
                .pmtctTreament(form.getPmtctTreament()).location(form.getLocation()).house(form.getHouse()).community(form.getCommunity())
                .referred(form.getReferred()).maleInvolved(form.getMaleInvolved()).nextANCDate(form.getNextANCDate()).comments(form.getComments());
    }
}
