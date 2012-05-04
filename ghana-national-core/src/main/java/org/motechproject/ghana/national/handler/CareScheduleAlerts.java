package org.motechproject.ghana.national.handler;

import org.motechproject.ghana.national.repository.AllMobileMidwifeEnrollments;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.repository.SMSGateway;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.model.MotechEvent;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.scheduletracking.api.events.MilestoneEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.ghana.national.domain.SmsTemplateKeys.*;

@Component
public class CareScheduleAlerts extends BaseScheduleHandler {

    public CareScheduleAlerts() {
    }

    @Autowired
    public CareScheduleAlerts(PatientService patientService, FacilityService facilityService,
                              SMSGateway smsGateway, AllObservations allObservations, AllMobileMidwifeEnrollments allMobileMidwifeEnrollments) {
        super(patientService, facilityService, smsGateway, allObservations, allMobileMidwifeEnrollments);
    }

    @LoginAsAdmin
    @ApiSession
    public void handlePregnancyAlert(final MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(PREGNANCY_ALERT_SMS_KEY, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleTTVaccinationAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(TT_VACCINATION_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_TT, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleBCGAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(BCG_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_BCG, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleAncVisitAlert(MotechEvent motechEvent) {
        sendAggregatedSMSToFacilityForAnAppointment(ANC_VISIT_SMS_KEY, motechEvent);
        sendAggregatedSMSToPatientForAppointment(PATIENT_ANC_VISIT, motechEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleIPTpVaccinationAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(ANC_IPTp_VACCINATION_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_IPT, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleIPTiVaccinationAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(CWC_IPTi_VACCINATION_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_IPTI, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleMeaslesVaccinationAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(CWC_MEASLES_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_MEASLES, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handlePentaVaccinationAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(CWC_PENTA_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_PENTA, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleYellowFeverVaccinationAlert(MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(CWC_YF_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_YELLOW_FEVER, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handleOpvVaccinationAlert(final MilestoneEvent milestoneEvent) {
        sendAggregatedSMSToFacility(CWC_OPV_SMS_KEY, milestoneEvent);
        sendAggregatedSMSToPatient(PATIENT_OPV, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handlePNCMotherAlert(final MilestoneEvent milestoneEvent) {
        sendInstantSMSToFacility(PNC_MOTHER_SMS_KEY, milestoneEvent);
        sendInstantSMSToPatient(PATIENT_PNC_MOTHER, milestoneEvent);
    }

    @LoginAsAdmin
    @ApiSession
    public void handlePNCChildAlert(final MilestoneEvent milestoneEvent) {
        sendInstantSMSToFacility(PNC_CHILD_SMS_KEY, milestoneEvent);
        sendInstantSMSToFacility(PATIENT_PNC_BABY, milestoneEvent);
    }
}
