package org.motechproject.ghana.national.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.repository.*;
import org.motechproject.ghana.national.service.request.PregnancyTerminationRequest;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.util.DateUtil;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.domain.Concept.*;
import static org.motechproject.ghana.national.domain.Constants.OTHER_CAUSE_OF_DEATH;
import static org.motechproject.ghana.national.domain.Constants.PREGNANCY_TERMINATION;
import static org.motechproject.ghana.national.domain.EncounterType.PREG_TERM_VISIT;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class PregnancyTerminationServiceTest {
    private PregnancyTerminationService pregnancyTerminationService;
    @Mock
    private AllPatients mockAllPatients;
    @Mock
    private AllEncounters mockAllEncounters;
    @Mock
    private AllFacilities mockAllFacilities;
    @Mock
    private AllSchedules mockAllSchedules;
    @Mock
    private AllAppointments mockAllAppointments;

    @Before
    public void setUp() {
        initMocks(this);
        pregnancyTerminationService = new PregnancyTerminationService(mockAllPatients, mockAllEncounters, mockAllFacilities, mockAllSchedules, mockAllAppointments);
    }

    @Test
    public void shouldDeceasePatientIfDeadDuringPregnancyTermination() {
        String motechId = "motechId";
        String staffId = "staffId";
        String facilityId = "facilityId";
        String mrsFacilityId = "mrsFacilityId";
        String patientMRSId = "patientMRSId";
        List<String> schedules = Arrays.asList("schedule1", "schedule2");
        PregnancyTerminationRequest request = pregnancyTermination(motechId, staffId, facilityId);
        request.setDead(Boolean.TRUE);

        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        when(mockMRSPatient.getId()).thenReturn("mrsPatientId");
        when(mockAllPatients.getPatientByMotechId(request.getMotechId())).thenReturn(mockPatient);
        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        when(mockPatient.getMRSPatientId()).thenReturn(patientMRSId);
        when(mockPatient.careProgramsToUnEnroll()).thenReturn(schedules);

        Facility mockFacility=mock(Facility.class);
        when(mockAllFacilities.getFacilityByMotechId(facilityId)).thenReturn(mockFacility);
        when(mockFacility.getMrsFacilityId()).thenReturn(mrsFacilityId);

        pregnancyTerminationService.terminatePregnancy(request);

        verify(mockAllPatients).deceasePatient(request.getTerminationDate(), request.getMotechId(), OTHER_CAUSE_OF_DEATH, PREGNANCY_TERMINATION);
        verify(mockAllSchedules).unEnroll(patientMRSId, schedules);
        verify(mockAllAppointments).remove(mockPatient);
    }

    @Test
    public void shouldCreateEncounterOnPregnancyTermination() {
        String staffId = "staffId";
        String facilityId = "facilityId";
        String motechId = "motechId";
        String mrsFacilityId = "mrsFacilityId";
        final Date date = DateUtil.newDate(2000, 12, 12).toDate();
        final PregnancyTerminationRequest request = pregnancyTermination(motechId, staffId, facilityId);
        request.setTerminationDate(date);

        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        when(mockAllPatients.getPatientByMotechId(request.getMotechId())).thenReturn(mockPatient);
        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);

        Facility mockFacility=mock(Facility.class);
        when(mockAllFacilities.getFacilityByMotechId(facilityId)).thenReturn(mockFacility);
        when(mockFacility.getMrsFacilityId()).thenReturn(mrsFacilityId);

        pregnancyTerminationService.terminatePregnancy(request);
        ArgumentCaptor<Set> captor = ArgumentCaptor.forClass(Set.class);

        verify(mockAllEncounters).persistEncounter(eq(mockMRSPatient), eq(staffId), eq(mrsFacilityId), eq(PREG_TERM_VISIT.value()), eq(date), captor.capture());

        Set observations = captor.getValue();

        Set<MRSObservation> expectedObservations = new HashSet<MRSObservation>() {{
            add(new MRSObservation<Boolean>(date, PREGNANCY_STATUS.getName(), false));
            add(new MRSObservation<Integer>(date, TERMINATION_TYPE.getName(), 2));
            add(new MRSObservation<Integer>(date, TERMINATION_PROCEDURE.getName(), 1));
            add(new MRSObservation<Integer>(date, TERMINATION_COMPLICATION.getName(), 1));
            add(new MRSObservation<Integer>(date, TERMINATION_COMPLICATION.getName(), 2));
            add(new MRSObservation<Boolean>(date, MATERNAL_DEATH.getName(), false));
            add(new MRSObservation<Boolean>(date, REFERRED.getName(), false));
            add(new MRSObservation<String>(date, COMMENTS.getName(), "Patient lost lot of blood"));
            add(new MRSObservation<Boolean>(date, POST_ABORTION_FP_COUNSELING.getName(), Boolean.FALSE));
            add(new MRSObservation<Boolean>(date, POST_ABORTION_FP_ACCEPTED.getName(), Boolean.TRUE));
        }};

        assertReflectionEquals(expectedObservations, observations, ReflectionComparatorMode.LENIENT_ORDER);
    }

    private PregnancyTerminationRequest pregnancyTermination(String motechId, String staffId, String facilityId) {
        PregnancyTerminationRequest request = new PregnancyTerminationRequest();
        request.setFacilityId(facilityId);
        request.setMotechId(motechId);
        request.setStaffId(staffId);
        request.setDead(Boolean.FALSE);
        request.setReferred(Boolean.FALSE);
        request.setTerminationProcedure("1");
        request.setTerminationType("2");
        request.setComments("Patient lost lot of blood");
        request.addComplication("1");
        request.addComplication("2");
        request.setPostAbortionFPAccepted(Boolean.TRUE);
        request.setPostAbortionFPCounselling(Boolean.FALSE);
        return request;
    }

}
