package org.motechproject.ghana.national.service;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDate;
import org.motechproject.ghana.national.domain.*;
import org.motechproject.ghana.national.factory.MotherVisitEncounterFactory;
import org.motechproject.ghana.national.factory.TTVaccinationVisitEncounterFactory;
import org.motechproject.ghana.national.mapper.ScheduleEnrollmentMapper;
import org.motechproject.ghana.national.mapper.TTVaccinationEnrollmentMapper;
import org.motechproject.ghana.national.repository.AllAppointments;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.repository.AllSchedules;
import org.motechproject.ghana.national.service.request.ANCVisitRequest;
import org.motechproject.mrs.model.MRSEncounter;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.model.MRSUser;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.motechproject.ghana.national.configuration.ScheduleNames.DELIVERY;
import static org.motechproject.ghana.national.domain.IPTVaccine.createFromANCVisit;
import static org.motechproject.ghana.national.vo.Pregnancy.basedOnDeliveryDate;
import static org.motechproject.util.DateUtil.newDate;
import static org.motechproject.util.DateUtil.today;

@Service
public class MotherVisitService extends VisitService {

    private AllEncounters allEncounters;
    private AllObservations allObservations;
    private AllAppointments allAppointments;
    MotherVisitEncounterFactory factory;

    @Autowired
    public MotherVisitService(AllEncounters allEncounters, AllObservations allObservations, AllSchedules allSchedules, AllAppointments allAppointments) {
        super(allSchedules);
        this.allEncounters = allEncounters;
        this.allObservations = allObservations;
        this.allAppointments = allAppointments;
        factory = new MotherVisitEncounterFactory();
    }

    public MRSEncounter registerANCVisit(ANCVisitRequest ancVisit) {
        Set<MRSObservation> mrsObservations = factory.createMRSObservations(ancVisit);
        updateEDD(ancVisit, mrsObservations);
        updateIPT(ancVisit, mrsObservations);
        updateANCVisit(ancVisit);
        return allEncounters.persistEncounter(factory.createEncounter(ancVisit, mrsObservations));
    }

    private void updateIPT(ANCVisitRequest ancVisit, Set<MRSObservation> mrsObservations) {
        IPTVaccine iptVaccine = createFromANCVisit(ancVisit);
        if (iptVaccine != null) {
            mrsObservations.addAll(factory.createObservationsForIPT(iptVaccine));
            enrollOrFulfillScheduleIPTp(ancVisit, iptVaccine);
        }
    }

    private void updateEDD(ANCVisitRequest ancVisit, Set<MRSObservation> mrsObservations) {
        Set<MRSObservation> eddObservations = allObservations.updateEDD(ancVisit.getEstDeliveryDate(), ancVisit.getPatient(), ancVisit.getStaff().getId());
        if (CollectionUtils.isNotEmpty(eddObservations)) {
            mrsObservations.addAll(eddObservations);
            EnrollmentRequest enrollmentRequest = new ScheduleEnrollmentMapper().map(ancVisit.getPatient(),
                    new PatientCare(DELIVERY, basedOnDeliveryDate(newDate(ancVisit.getEstDeliveryDate())).dateOfConception()), null);
            allSchedules.enroll(enrollmentRequest);
        }
    }

    private void updateANCVisit(ANCVisitRequest ancVisitRequest) {
        allAppointments.updateANCVisitSchedule(ancVisitRequest.getPatient(), DateUtil.newDateTime(ancVisitRequest.getNextANCDate()));
    }

    public void receivedTT(final TTVaccineDosage dosage, Patient patient, MRSUser staff, Facility facility, final LocalDate vaccinationDate) {
        TTVisit ttVisit = new TTVisit().dosage(dosage).facility(facility).patient(patient).staff(staff).date(vaccinationDate.toDate());
        Encounter encounter = new TTVaccinationVisitEncounterFactory().createEncounterForVisit(ttVisit);
        allEncounters.persistEncounter(encounter);
        final EnrollmentRequest enrollmentRequest = new TTVaccinationEnrollmentMapper().map(patient, vaccinationDate, dosage.getScheduleMilestoneName(), today());
        allSchedules.enrollOrFulfill(enrollmentRequest, newDate(ttVisit.getDate()));
    }

    private void enrollOrFulfillScheduleIPTp(ANCVisitRequest ancVisit, IPTVaccine iptVaccine) {
        Patient patient = iptVaccine.getGivenTo();
        LocalDate visitDate = newDate(ancVisit.getDate());
        EnrollmentRequest enrollmentOrFulfillRequest = new ScheduleEnrollmentMapper().map(patient, patient.iptPatientCareEnrollOnVisitAfter19Weeks(visitDate), visitDate, iptVaccine.getIptMilestone());
        allSchedules.enrollOrFulfill(enrollmentOrFulfillRequest, visitDate);
    }
}
