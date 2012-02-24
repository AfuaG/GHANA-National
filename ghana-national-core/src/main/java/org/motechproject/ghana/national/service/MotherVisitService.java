package org.motechproject.ghana.national.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;
import org.motechproject.ghana.national.domain.*;
import org.motechproject.ghana.national.factory.MotherVisitEncounterFactory;
import org.motechproject.ghana.national.factory.TTVaccinationVisitEncounterFactory;
import org.motechproject.ghana.national.mapper.ScheduleEnrollmentMapper;
import org.motechproject.ghana.national.mapper.TTVaccinationEnrollmentMapper;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.vo.ANCVisit;
import org.motechproject.mrs.model.MRSEncounter;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.model.MRSUser;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.motechproject.ghana.national.configuration.ScheduleNames.DELIVERY;
import static org.motechproject.ghana.national.configuration.ScheduleNames.TT_VACCINATION_VISIT;
import static org.motechproject.ghana.national.domain.Concept.EDD;
import static org.motechproject.ghana.national.domain.Concept.PREGNANCY;
import static org.motechproject.ghana.national.vo.Pregnancy.basedOnDeliveryDate;

@Service
public class MotherVisitService extends BaseScheduleService {

    private EncounterService encounterService;
    AllObservations allObservations;

    private Logger logger = Logger.getLogger(MotherVisitEncounterFactory.class);

    @Autowired
    public MotherVisitService(EncounterService encounterService, ScheduleTrackingService scheduleTrackingService,
                              AllObservations allObservations) {
        super(scheduleTrackingService);
        this.encounterService = encounterService;
        this.allObservations = allObservations;
    }

    public MRSEncounter registerANCVisit(ANCVisit ancVisit) {
        MotherVisitEncounterFactory factory = new MotherVisitEncounterFactory();

        Set<MRSObservation> mrsObservations = factory.createMRSObservations(ancVisit);
        Set<MRSObservation> eddObservations = createEDDObservationsForANCVisit(ancVisit);
        if (CollectionUtils.isNotEmpty(eddObservations)) {
            mrsObservations.addAll(eddObservations);
            createEDDScheduleForANCVisit(ancVisit.getPatient(), ancVisit.getEstDeliveryDate());
        }
        return encounterService.persistEncounter(factory.createEncounter(ancVisit, mrsObservations));
    }

    public Set<MRSObservation> createEDDObservationsForANCVisit(final ANCVisit ancVisit) {
        HashSet<MRSObservation> observations = new HashSet<MRSObservation>();
        Date newEdd = ancVisit.getEstDeliveryDate();
        Patient patient = ancVisit.getPatient();
        String motechId = patient.getMotechId();

        if (newEdd == null) {
            return observations;
        }

        MRSObservation activePregnancyObservation = allObservations.findObservation(motechId, PREGNANCY.getName());
        if (activePregnancyObservation == null) {
            logger.warn("No active pregnancy found while checking for EDD. Patient ID :" + patient.getMrsPatient().getMotechId());
            return observations; //no active pregnancy
        }

        MRSObservation eddObservation = allObservations.findObservation(motechId, EDD.getName());
        Date oldEdd = (eddObservation == null) ? null : (Date) eddObservation.getValue();

        if (oldEdd == null || !oldEdd.equals(newEdd)) {
            observations.add(createNewEddObservation(ancVisit, activePregnancyObservation, eddObservation));
        }
        return observations;
    }

    private MRSObservation createNewEddObservation(final ANCVisit ancVisit, MRSObservation activePregnancyObservation, MRSObservation eddObservation) {
        allObservations.voidObservation(eddObservation, "Replaced by new EDD value", ancVisit.getStaff().getId());
        activePregnancyObservation.setDependantObservations(new HashSet<MRSObservation>() {{
            add(new MRSObservation<Date>(new Date(), EDD.getName(), ancVisit.getEstDeliveryDate()));
        }});
        return activePregnancyObservation;
    }

    void createEDDScheduleForANCVisit(Patient patient, Date estimatedDateOfDelivery) {
        EnrollmentRequest enrollmentRequest = new ScheduleEnrollmentMapper().map(patient, new PatientCare(DELIVERY, basedOnDeliveryDate(DateUtil.newDate(estimatedDateOfDelivery)).dateOfConception()));
        enroll(enrollmentRequest);
    }

    public void receivedTT(final TTVaccineDosage dosage, Patient patient, MRSUser staff, Facility facility, final LocalDate vaccinationDate) {
        TTVisit ttVisit = new TTVisit().dosage(dosage).facility(facility).patient(patient).staff(staff).date(vaccinationDate.toDate());
        Encounter encounter = new TTVaccinationVisitEncounterFactory().createEncounterForVisit(ttVisit);
        encounterService.persistEncounter(encounter);
        final EnrollmentRequest enrollmentRequest = new TTVaccinationEnrollmentMapper().map(patient, vaccinationDate, dosage.getScheduleMilestoneName());
        enrollOrFulfill(patient, enrollmentRequest);
    }

    public void unScheduleAll(Patient patient) {
        scheduleTrackingService.unenroll(patient.getMRSPatientId(), TT_VACCINATION_VISIT);
    }
}
