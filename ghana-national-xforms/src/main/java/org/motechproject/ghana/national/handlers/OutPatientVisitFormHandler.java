package org.motechproject.ghana.national.handlers;

import org.motechproject.ghana.national.bean.OutPatientVisitForm;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.OutPatientVisit;
import org.motechproject.ghana.national.exception.XFormHandlerException;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.OutPatientVisitService;
import org.motechproject.mrs.model.MRSConcept;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.services.MRSPatientAdapter;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static org.motechproject.ghana.national.domain.Concept.*;
import static org.motechproject.ghana.national.domain.EncounterType.OUTPATIENT_VISIT;

@Component
public class OutPatientVisitFormHandler{

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AllEncounters allEncounters;

    @Autowired
    FacilityService facilityService;

    @Autowired
    MRSPatientAdapter patientAdapter;

    public static Integer OTHER_DIAGNOSIS = 78;

    @Autowired
    OutPatientVisitService outPatientVisitService;

    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(OutPatientVisitForm outPatientVisitForm) {
        try {
            if (Boolean.FALSE.equals(outPatientVisitForm.isRegistered())) {
                persist(outPatientVisitForm);
            } else {
                persistInMRS(outPatientVisitForm);
            }
        } catch (Exception e) {
            log.error("Exception occurred while processing Outpatient Visit form for patientId:"+outPatientVisitForm.getMotechId(), e);
            throw new XFormHandlerException("Exception occurred while processing Outpatient Visit form", e);
        }
    }

    private void persist(OutPatientVisitForm formBean) {
        OutPatientVisit visit = new OutPatientVisit();
        Integer diagnosis = OTHER_DIAGNOSIS.equals(formBean.getDiagnosis()) ? formBean.getOtherDiagnosis() : formBean.getDiagnosis();
        Integer secondaryDiagnosis=null;
        if(formBean.getSecondDiagnosis() != null){
         secondaryDiagnosis= OTHER_DIAGNOSIS.equals(formBean.getSecondDiagnosis()) ? formBean.getOtherSecondaryDiagnosis() : formBean.getSecondDiagnosis();
        }
        visit.setVisitDate(formBean.getVisitDate()).setRegistrantType(formBean.getRegistrantType()).setSerialNumber(formBean.getSerialNumber()).setFacilityId(formBean.getFacilityId())
                .setStaffId(formBean.getStaffId()).setDateOfBirth(formBean.getDateOfBirth()).setInsured(formBean.getInsured()).setNhis(formBean.getNhis()).setNhisExpires(formBean.getNhisExpires())
                .setDiagnosis(diagnosis).setNewCase(formBean.getNewCase()).setNewPatient(formBean.getNewPatient()).setSecondDiagnosis(secondaryDiagnosis).setRdtGiven(formBean.getRdtGiven()).setRdtPositive(formBean.getRdtPositive())
                .setActTreated(formBean.getActTreated()).setReferred(formBean.getReferred()).setComments(formBean.getComments()).setGender(formBean.getGender());
        outPatientVisitService.registerVisit(visit);
    }

    private void persistInMRS(OutPatientVisitForm formBean) {
        MRSPatient mrsPatient = patientAdapter.getPatientByMotechId(formBean.getMotechId());
        Set<MRSObservation> observationList = new HashSet<MRSObservation>();


        if (formBean.getNewCase() != null) {
            MRSObservation newCaseObs = new MRSObservation<Boolean>(formBean.getVisitDate(), NEW_CASE.getName(), formBean.getNewCase());
            observationList.add(newCaseObs);
        }
        if (formBean.getNewPatient() != null) {
            MRSObservation newPatientObs = new MRSObservation<Boolean>(formBean.getVisitDate(), NEW_PATIENT.getName(), formBean.getNewPatient());
            observationList.add(newPatientObs);
        }
        if (formBean.getDiagnosis() != null) {
            int obsVal = formBean.getDiagnosis().equals(OTHER_DIAGNOSIS) ? formBean.getOtherDiagnosis() : formBean.getDiagnosis();
            MRSObservation diagnosisObs = new MRSObservation<Integer>(formBean.getVisitDate(), PRIMARY_DIAGNOSIS.getName(), obsVal);
            observationList.add(diagnosisObs);

        }
        if (formBean.getSecondDiagnosis() != null) {
            int obsVal = formBean.getSecondDiagnosis().equals(OTHER_DIAGNOSIS) ? formBean.getOtherSecondaryDiagnosis() : formBean.getSecondDiagnosis();
            MRSObservation secondDiagnosisObs = new MRSObservation<Integer>(formBean.getVisitDate(), SECONDARY_DIAGNOSIS.getName(), obsVal);
            observationList.add(secondDiagnosisObs);

        }
        if (formBean.getReferred() != null) {
            MRSObservation referredObs = new MRSObservation<Boolean>(formBean.getVisitDate(), REFERRED.getName(), formBean.getReferred());
            observationList.add(referredObs);

        }
        if (formBean.getComments() != null) {
            MRSObservation commentsObs = new MRSObservation<String>(formBean.getVisitDate(), COMMENTS.getName(), formBean.getComments());
            observationList.add(commentsObs);

        }

        if (formBean.getSerialNumber() != null) {
            MRSObservation serialNumberObs = new MRSObservation<String>(formBean.getVisitDate(), SERIAL_NUMBER.getName(), formBean.getSerialNumber());
            observationList.add(serialNumberObs);
        }

        if (Boolean.TRUE.equals(formBean.getRdtGiven())) {
            MRSObservation rdtGivenObs;
            if (Boolean.TRUE.equals(formBean.getRdtPositive()))
                rdtGivenObs = new MRSObservation<MRSConcept>(formBean.getVisitDate(), MALARIA_RAPID_TEST.getName(), new MRSConcept(POSITIVE.getName()));
            else
                rdtGivenObs = new MRSObservation<MRSConcept>(formBean.getVisitDate(), MALARIA_RAPID_TEST.getName(), new MRSConcept(NEGATIVE.getName()));
            observationList.add(rdtGivenObs);
        }

        if (formBean.getActTreated() != null) {
            MRSObservation actTreatedObs = new MRSObservation<Boolean>(formBean.getVisitDate(), ACT_TREATMENT.getName(), formBean.getActTreated());
            observationList.add(actTreatedObs);
        }

        Facility facility = facilityService.getFacilityByMotechId(formBean.getFacilityId());
        allEncounters.persistEncounter(mrsPatient, formBean.getStaffId(), facility.mrsFacilityId(), OUTPATIENT_VISIT.value(),
                formBean.getVisitDate(), observationList);
    }
}
