package org.motechproject.ghana.national.service;

import org.apache.commons.collections.CollectionUtils;
import org.joda.time.LocalDate;
import org.motechproject.ghana.national.domain.IPTVaccine;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientCare;
import org.motechproject.ghana.national.domain.TTVaccine;
import org.motechproject.ghana.national.factory.MotherVisitEncounterFactory;
import org.motechproject.ghana.national.mapper.ScheduleEnrollmentMapper;
import org.motechproject.ghana.national.repository.AllAppointments;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.repository.AllSchedules;
import org.motechproject.ghana.national.service.request.ANCVisitRequest;
import org.motechproject.mrs.model.MRSEncounter;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

import static org.motechproject.ghana.national.configuration.ScheduleNames.ANC_DELIVERY;
import static org.motechproject.ghana.national.domain.IPTVaccine.createFromANCVisit;
import static org.motechproject.ghana.national.vo.Pregnancy.basedOnDeliveryDate;
import static org.motechproject.util.DateUtil.newDate;

@Service
public class MotherVisitService {

    private AllSchedules allSchedules;
    private AllEncounters allEncounters;
    private AllObservations allObservations;
    private AllAppointments allAppointments;
    private MotherVisitEncounterFactory factory;
    private VisitService visitService;

    @Autowired
    public MotherVisitService(AllEncounters allEncounters, AllObservations allObservations, AllSchedules allSchedules, AllAppointments allAppointments, VisitService visitService) {
        this.allEncounters = allEncounters;
        this.allObservations = allObservations;
        this.allAppointments = allAppointments;
        this.allSchedules = allSchedules;
        this.visitService = visitService;
        factory = new MotherVisitEncounterFactory();
    }

    public MRSEncounter registerANCVisit(ANCVisitRequest ancVisit) {
        Set<MRSObservation> mrsObservations = factory.createMRSObservations(ancVisit);
        updateEDD(ancVisit, mrsObservations);
        updateIPT(ancVisit, mrsObservations);
        updateTT(ancVisit, mrsObservations);
        updateANCVisit(ancVisit);
        return allEncounters.persistEncounter(factory.createEncounter(ancVisit, mrsObservations));
    }

    protected void updateTT(ANCVisitRequest ancVisit, Set<MRSObservation> mrsObservations) {
        TTVaccine ttVaccine = TTVaccine.createFromANCVisit(ancVisit);
        if (ttVaccine != null) {
            mrsObservations.addAll(factory.createObservationForTT(ttVaccine));
            visitService.createTTSchedule(ttVaccine);
        }
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
                    new PatientCare(ANC_DELIVERY, basedOnDeliveryDate(newDate(ancVisit.getEstDeliveryDate())).dateOfConception()), null);
            allSchedules.enroll(enrollmentRequest);
        }
    }

    private void updateANCVisit(ANCVisitRequest ancVisitRequest) {
        allAppointments.updateANCVisitSchedule(ancVisitRequest.getPatient(), ancVisitRequest.getDate(), DateUtil.newDateTime(ancVisitRequest.getNextANCDate()));
    }

    private void enrollOrFulfillScheduleIPTp(ANCVisitRequest ancVisit, IPTVaccine iptVaccine) {
        Patient patient = iptVaccine.getGivenTo();
        LocalDate visitDate = newDate(ancVisit.getDate());
        EnrollmentRequest enrollmentOrFulfillRequest = new ScheduleEnrollmentMapper().map(patient, patient.ancIPTPatientCareEnrollOnVisitAfter19Weeks(visitDate), visitDate, iptVaccine.getIptMilestone());
        allSchedules.enrollOrFulfill(enrollmentOrFulfillRequest, visitDate);
    }
}
