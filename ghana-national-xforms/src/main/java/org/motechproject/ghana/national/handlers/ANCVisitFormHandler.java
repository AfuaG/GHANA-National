package org.motechproject.ghana.national.handlers;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ghana.national.bean.ANCVisitForm;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.TTVaccineDosage;
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
import org.motechproject.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.ghana.national.domain.Constants.FORM_BEAN;
import static org.motechproject.ghana.national.domain.Constants.NOT_APPLICABLE;

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

    @Override
    @MotechListener(subjects = "form.validation.successful.NurseDataEntry.ancVisit")
    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(MotechEvent event) {
        try {
            ANCVisitForm form = (ANCVisitForm) event.getParameters().get(FORM_BEAN);
            ANCVisitRequest ancVisitRequest = createANCVisit(form);
            visitService.registerANCVisit(ancVisitRequest);
            if (!StringUtils.isEmpty(ancVisitRequest.getTtdose()) && !ancVisitRequest.getTtdose().equals(NOT_APPLICABLE)) {
                visitService.receivedTT(TTVaccineDosage.byValue(Integer.parseInt(ancVisitRequest.getTtdose())),
                        ancVisitRequest.getPatient(), ancVisitRequest.getStaff(), ancVisitRequest.getFacility(), DateUtil.today());
            }
        } catch (Exception e) {
            log.error("Encountered error while saving ANC visit details", e);
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
