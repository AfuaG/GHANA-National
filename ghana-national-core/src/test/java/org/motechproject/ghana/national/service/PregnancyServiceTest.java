package org.motechproject.ghana.national.service;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.*;
import org.motechproject.ghana.national.factory.PregnancyEncounterFactory;
import org.motechproject.ghana.national.repository.*;
import org.motechproject.ghana.national.service.request.DeliveredChildRequest;
import org.motechproject.ghana.national.service.request.PregnancyDeliveryRequest;
import org.motechproject.ghana.national.service.request.PregnancyTerminationRequest;
import org.motechproject.mrs.model.*;
import org.motechproject.util.DateUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.domain.Constants.OTHER_CAUSE_OF_DEATH;
import static org.motechproject.ghana.national.domain.Constants.PREGNANCY_TERMINATION;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class PregnancyServiceTest {
    private PregnancyService pregnancyService;
    @Mock
    private AllPatients mockAllPatients;
    @Mock
    private AllEncounters mockAllEncounters;
    @Mock
    private AllSchedules mockAllSchedules;
    @Mock
    private AllAppointments mockAllAppointments;
    @Mock
    private IdentifierGenerator mockIdentifierGenerator;

    @Mock
    private  AllObservations mockAllObservations;

    @Before
    public void setUp() {
        initMocks(this);
        pregnancyService = new PregnancyService(mockAllPatients, mockAllEncounters, mockAllSchedules, mockAllAppointments, mockIdentifierGenerator, mockAllObservations);
    }

    @Test
    public void shouldDeceasePatientIfDeadDuringPregnancyTermination() {
        String mrsFacilityId = "mrsFacilityId";
        String patientMRSId = "patientMRSId";
        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        when(mockMRSPatient.getId()).thenReturn("mrsPatientId");
        Facility mockFacility = mock(Facility.class);
        MRSUser mockStaff = mock(MRSUser.class);
        List<String> schedules = Arrays.asList("schedule1", "schedule2");
        PregnancyTerminationRequest request = pregnancyTermination(mockPatient, mockStaff, mockFacility);
        request.setDead(Boolean.TRUE);

        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        when(mockPatient.getMRSPatientId()).thenReturn(patientMRSId);
        when(mockPatient.ancCareProgramsToUnEnroll()).thenReturn(schedules);
        when(mockFacility.getMrsFacilityId()).thenReturn(mrsFacilityId);

        pregnancyService.terminatePregnancy(request);

        verify(mockAllPatients).deceasePatient(request.getTerminationDate(), request.getPatient().getMotechId(), OTHER_CAUSE_OF_DEATH, PREGNANCY_TERMINATION);
        verify(mockAllSchedules).unEnroll(patientMRSId, schedules);
        verify(mockAllAppointments).remove(mockPatient);
    }

    @Test
    public void shouldCreateEncounterOnPregnancyTermination() {
        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        Facility mockFacility = mock(Facility.class);
        MRSUser mockStaff = mock(MRSUser.class);

        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        final Date date = DateUtil.newDate(2000, 12, 12).toDate();
        PregnancyTerminationRequest request = pregnancyTermination(mockPatient, mockStaff, mockFacility);
        request.setTerminationDate(date);

        pregnancyService.terminatePregnancy(request);

        Encounter expectedEncounter = new PregnancyEncounterFactory().createTerminationEncounter(request);
        ArgumentCaptor<Encounter> argumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(mockAllEncounters).persistEncounter(argumentCaptor.capture());

        assertReflectionEquals(expectedEncounter, argumentCaptor.getValue());
    }

    @Test
    public void shouldCreateEncounterForPregnancyDelivery_AndForBirth() {
        MRSFacility mrsFacility = new MRSFacility("12");
        Facility mockFacility = new Facility(mrsFacility);
        MRSUser mockStaff = mock(MRSUser.class);
        String parentMotechId = "121";
        MRSPatient mrsPatient = new MRSPatient(parentMotechId, null, null);
        Patient mockPatient = new Patient(mrsPatient, parentMotechId);

        final String childMotechId = "child-motech-id";
        final String childWeight = "1.1";
        final String childSex = "?";
        final String childFirstName = "Jo";

        DeliveredChildRequest deliveredChildRequest = new DeliveredChildRequest()
                .childBirthOutcome(BirthOutcome.ALIVE)
                .childRegistrationType(RegistrationType.USE_PREPRINTED_ID).childFirstName(childFirstName)
                .childWeight(childWeight).childMotechId(childMotechId).childSex(childSex);

        PregnancyDeliveryRequest deliveryRequest = pregnancyDelivery(mockPatient, mockStaff, mockFacility, deliveredChildRequest);

        final Date birthDate = deliveryRequest.getDeliveryDateTime().toDate();

        final String childDefaultLastName = "Baby";
        MRSPatient childMRSPatient = new MRSPatient(childMotechId, new MRSPerson().firstName(childFirstName).lastName(childDefaultLastName).dateOfBirth(birthDate).gender("?").dead(false), mrsFacility);

        final MRSObservation activePregnancyObservation = new MRSObservation(new Date(), "PREG", "Value");

        when(mockAllObservations.activePregnancyObservation(parentMotechId)).thenReturn(activePregnancyObservation);
        pregnancyService.handleDelivery(deliveryRequest);

        final PregnancyEncounterFactory factory = new PregnancyEncounterFactory();
        Encounter expectedDeliveryEncounter = factory.createDeliveryEncounter(deliveryRequest, activePregnancyObservation);
        Encounter expectedBirthEncounter = factory.createBirthEncounter(deliveredChildRequest, childMRSPatient,mockStaff,mockFacility, birthDate);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(mockAllEncounters,times(2)).persistEncounter(encounterArgumentCaptor.capture());
        verify(mockAllPatients).save(patientArgumentCaptor.capture());

        List<Encounter> encounters = encounterArgumentCaptor.getAllValues();
        assertThat(encounters.size(), is(equalTo(2)));
        assertReflectionEquals(expectedBirthEncounter, encounters.get(0));
        assertReflectionEquals(expectedDeliveryEncounter, encounters.get(1));

        Patient actualChild = patientArgumentCaptor.getValue();
        assertThat(actualChild.getMotechId(), is(deliveryRequest.getDeliveredChildRequests().get(0).getChildMotechId()));
        assertThat(actualChild.getParentId(), is(parentMotechId));
        assertThat(actualChild.getFirstName(), is(childFirstName));
        assertThat(actualChild.getLastName(), is(childDefaultLastName));
        assertThat(actualChild.getMrsPatient().getFacility(), is(mrsFacility));
        verify(mockAllSchedules).unEnroll(mrsPatient.getId(), mockPatient.ancCareProgramsToUnEnroll());
        verify(mockAllAppointments).remove(mockPatient);
    }

    private PregnancyTerminationRequest pregnancyTermination(Patient mockPatient, MRSUser mockUser, Facility mockFacility) {
        PregnancyTerminationRequest request = new PregnancyTerminationRequest();
        request.setFacility(mockFacility);
        request.setPatient(mockPatient);
        request.setStaff(mockUser);
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

    private PregnancyDeliveryRequest pregnancyDelivery(Patient mockPatient, MRSUser mockUser, Facility mockFacility, DeliveredChildRequest deliveredChildRequest) {
        PregnancyDeliveryRequest request = new PregnancyDeliveryRequest();
        request.facility(mockFacility);
        request.patient(mockPatient);
        request.staff(mockUser);
        request.deliveryDateTime(DateTime.now());
        request.childDeliveryOutcome(ChildDeliveryOutcome.SINGLETON);
        request.addDeliveredChildRequest(deliveredChildRequest);

        return request;
    }
}
