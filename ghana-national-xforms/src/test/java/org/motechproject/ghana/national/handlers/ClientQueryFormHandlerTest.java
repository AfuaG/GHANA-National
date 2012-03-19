package org.motechproject.ghana.national.handlers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.ClientQueryForm;
import org.motechproject.ghana.national.domain.ClientQueryType;
import org.motechproject.ghana.national.domain.Concept;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientAttributes;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.repository.SMSGateway;
import org.motechproject.ghana.national.service.CareProgramQueryService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.*;
import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
import org.motechproject.util.DateUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.*;
import static org.motechproject.ghana.national.domain.Constants.*;
import static org.motechproject.ghana.national.handlers.ClientQueryFormHandler.FIND_CLIENT_RESPONSE_SMS_KEY;
import static org.motechproject.ghana.national.handlers.ClientQueryFormHandler.UPCOMING_CARE_CLIENT_QUERY_RESPONSE_SMS_KEY;
import static org.motechproject.ghana.national.util.AssertionUtility.assertContainsTemplateValues;
import static org.motechproject.util.DateUtil.newDateTime;

public class ClientQueryFormHandlerTest {
    private ClientQueryFormHandler clientQueryFormHandler;
    @Mock
    private PatientService mockPatientService;
    @Mock
    private SMSGateway mockSmsGateway;
    @Mock
    private AllObservations mockAllObservations;
    @Mock
    private CareProgramQueryService mockCareProgramQueryService;

    @Before
    public void setUp() {
        initMocks(this);
        clientQueryFormHandler = new ClientQueryFormHandler();
        ReflectionTestUtils.setField(clientQueryFormHandler, "patientService", mockPatientService);
        ReflectionTestUtils.setField(clientQueryFormHandler, "allObservations", mockAllObservations);
        ReflectionTestUtils.setField(clientQueryFormHandler, "smsGateway", mockSmsGateway);
        ReflectionTestUtils.setField(clientQueryFormHandler, "careProgramQueryService", mockCareProgramQueryService);
    }

    @Test
    public void shouldSendMessageWithEDDForPregnantClientForClientDetailsQuery() {
        final String firstName = "firstName";
        final String lastName = "lastName";
        final Date dateOfBirth = DateUtil.now().minusYears(20).toDate();
        final Integer age = 30;
        final String gender = "Male";
        final String phoneNumber = "phoneNumber";
        String responsePhoneNumber = "responsePhoneNumber";
        final Date edd = DateUtil.now().toDate();

        String facilityId = "facilityId";
        String motechId = "motechId";
        String staffId = "staffId";
        Map<String, Object> params = createMotechEventWithForm(facilityId, motechId, staffId, responsePhoneNumber, ClientQueryType.CLIENT_DETAILS.toString());

        MRSPerson person = person(phoneNumber, dateOfBirth, age, gender, lastName, firstName);
        Patient patient = new Patient(new MRSPatient(motechId, person, new MRSFacility(facilityId)));

        when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
        when(mockAllObservations.findObservation(motechId, Concept.PREGNANCY.getName())).thenReturn(mockPregnancyObservationWithEDD(edd));
        when(mockAllObservations.hasActivePregnancyStatusObservation(motechId)).thenReturn(Boolean.TRUE);

        clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));

        ArgumentCaptor<Map> templateValuesCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockSmsGateway).dispatchSMS(eq("PREGNANT_CLIENT_QUERY_RESPONSE_SMS_KEY"), templateValuesCaptor.capture(), eq(responsePhoneNumber));

        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(PHONE_NUMBER, phoneNumber);
            put(AGE, age.toString());
            put(GENDER, gender);
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastName);
            put(DATE, new SimpleDateFormat(PATTERN_DD_MMM_YYYY).format(edd));
            put(DOB, new SimpleDateFormat(PATTERN_DD_MMM_YYYY).format(dateOfBirth));

        }}, templateValuesCaptor.getValue());


    }

    @Test
    public void shouldSendMessageForFindClientIDQuery() {
        final String firstName = "firstName";
        final String lastName = "lastName";
        final Date dateOfBirth = DateUtil.now().minusYears(20).toDate();
        String dateString = new SimpleDateFormat(PATTERN_DD_MMM_YYYY).format(dateOfBirth);
        String phoneNumber = "phoneNumber";
        String responsePhoneNumber = "responsePhoneNumber";
        final String facilityId = "facilityId";
        final String motechId = "motechId";
        final MRSFacility mrsFacility = new MRSFacility(facilityId, "name", null, null, null, null);

        HashMap<String, Object> params = createClientQueryFormForFindClientId(firstName, lastName, dateOfBirth, phoneNumber, responsePhoneNumber, facilityId, motechId, ClientQueryType.FIND_CLIENT_ID);

        ArrayList<MRSPatient> patients = new ArrayList<MRSPatient>() {{
            add(new MRSPatient(motechId, new MRSPerson().lastName(lastName).firstName(firstName).dateOfBirth(dateOfBirth).gender("F"), mrsFacility));
            add(new MRSPatient("45423", new MRSPerson().lastName(lastName).firstName("first").gender("M").dateOfBirth(new Date(189, 5, 6)), mrsFacility));
        }};
        when(mockPatientService.getPatients(firstName, lastName, phoneNumber, dateOfBirth, null)).thenReturn(patients);
        when(mockSmsGateway.getSMSTemplate(FIND_CLIENT_RESPONSE_SMS_KEY)).thenReturn("MoTeCH ID=${motechId},${firstName},${lastName}, Sex=${gender}, DoB=${dob}, ${facility}");

        clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));

        ArgumentCaptor<String> templateValuesCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSmsGateway).dispatchSMS(eq(responsePhoneNumber), templateValuesCaptor.capture());

        String expectedMessage = "MoTeCH ID=motechId,firstName,lastName, Sex=F, DoB=" + dateString + ", name%0aMoTeCH ID=45423,first,lastName, Sex=M, DoB=06 Jun 2089, name%0a";
        assertEquals(expectedMessage, templateValuesCaptor.getValue());
    }

    @Test
    public void shouldSendNoMatchingRecordsFoundIfNoRecordsFound() {
        final String firstName = "firstName";
        final String lastName = "lastName";
        final Date dateOfBirth = DateUtil.now().minusYears(20).toDate();
        String phoneNumber = "phoneNumber";
        String responsePhoneNumber = "responsePhoneNumber";
        final String facilityId = "facilityId";

        HashMap<String, Object> params = createClientQueryFormForFindClientId(firstName, lastName, dateOfBirth, phoneNumber, responsePhoneNumber, facilityId, null, ClientQueryType.FIND_CLIENT_ID);

        when(mockPatientService.getPatients(firstName, lastName, phoneNumber, dateOfBirth, null)).thenReturn(new ArrayList<MRSPatient>());
        when(mockSmsGateway.getSMSTemplate(FIND_CLIENT_RESPONSE_SMS_KEY)).thenReturn("MoTeCH ID=${motechId}, FirstName=${firstName}, LastName=${lastName}, Sex=${gender}, DoB=${dob}, Facility=${facility}");

        clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));

        ArgumentCaptor<String> templateValuesCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockSmsGateway).dispatchSMS(eq(responsePhoneNumber), templateValuesCaptor.capture());

        assertEquals(NO_MATCHING_RECORDS_FOUND, templateValuesCaptor.getValue());
    }

    @Test
    public void shouldSendUpcomingCareDetailsIfQueryTypeIsUpcomingCare() {
        final String motechId = "motech-id";
        String responsePhoneNumber = "94423232";
        final String facilityId = "facilityId";
        final String firstName = "Sachin";
        final String lastName = "A";

        HashMap<String, Object> params = createClientQueryFormForFindClientId(null, null, null, null, responsePhoneNumber, facilityId, motechId, ClientQueryType.UPCOMING_CARE);

        final Patient patient = new Patient(new MRSPatient(motechId, new MRSPerson().gender("M").firstName(firstName).lastName(lastName)
                .dateOfBirth(newDateTime(2012, 4, 5, 2, 2, 0).toDate()), null));
        EnrollmentRecord ttEnrollmentRecord = new EnrollmentRecord("", "TT schedule", "", null, null, null, null, newDateTime(2012, 3, 3, 1, 1, 0), null, null);
        EnrollmentRecord iptEnrollmentRecord = new EnrollmentRecord("", "IPT schedule", "", null, null, null, null, newDateTime(2012, 4, 5, 2, 2, 0), null, null);
        List<EnrollmentRecord> upcomingEnrollments = asList(ttEnrollmentRecord, iptEnrollmentRecord);
        when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
        when(mockCareProgramQueryService.upcomingCareProgramsForCurrentWeek(patient)).thenReturn(upcomingEnrollments);
        when(mockSmsGateway.getSMSTemplate(UPCOMING_CARE_CLIENT_QUERY_RESPONSE_SMS_KEY))
                .thenReturn("Upcoming care programs for ${firstName} ${lastName} (MotechID:${motechId}):\\n $T{UPCOMING_CARE_DUE_CLIENT_QUERY}.");
        when(mockSmsGateway.getSMSTemplate(ClientQueryFormHandler.UPCOMING_CARE_DUE_CLIENT_QUERY))
                .thenReturn("${scheduleName}: ${date}");

        clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));
        String expectedMsg = format("Upcoming care programs for %s %s (MotechID:%s):\\n %s."
                ,firstName, lastName, motechId, "TT schedule: 03 Mar 2012, IPT schedule: 05 Apr 2012");
        verify(mockSmsGateway).dispatchSMS(eq(responsePhoneNumber), eq(expectedMsg));
    }

    @Test
    public void shouldSendNoResultsFoundIfNoUpcomingCareDetailsFound() {
        final String motechId = "motech-id";
        String responsePhoneNumber = "94423232";
        final String facilityId = "facilityId";
        final String firstName = "Sachin";
        final String lastName = "A";

        HashMap<String, Object> params = createClientQueryFormForFindClientId(null, null, null, null, responsePhoneNumber, facilityId, motechId, ClientQueryType.UPCOMING_CARE);

        final Patient patient = new Patient(new MRSPatient(motechId, new MRSPerson().gender("M").firstName(firstName).lastName(lastName)
                .dateOfBirth(newDateTime(2012, 4, 5, 2, 2, 0).toDate()), null));
        List<EnrollmentRecord> upcomingEnrollments = Collections.emptyList();
        when(mockPatientService.getPatientByMotechId(motechId)).thenReturn(patient);
        when(mockCareProgramQueryService.upcomingCareProgramsForCurrentWeek(patient)).thenReturn(upcomingEnrollments);
        when(mockSmsGateway.getSMSTemplate(ClientQueryFormHandler.NONE_UPCOMING)).thenReturn("None upcoming for MotechID:${motechId}");

        clientQueryFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseQuery.clientQuery", params));
        String expectedMsg = "None upcoming for MotechID:%s".format(motechId);
        mockSmsGateway.dispatchSMS(eq(responsePhoneNumber), eq(expectedMsg));
    }

    private HashMap<String, Object> createClientQueryFormForFindClientId(String firstName, String lastName, Date dateOfBirth, String phoneNumber, String responsePhoneNumber, String facilityId, String motechId, ClientQueryType clientQueryType) {
        final ClientQueryForm clientQueryForm = clientQueryForm(facilityId, motechId, "323", responsePhoneNumber, clientQueryType.toString());
        clientQueryForm.setFirstName(firstName);
        clientQueryForm.setLastName(lastName);
        clientQueryForm.setDateOfBirth(dateOfBirth);
        clientQueryForm.setPhoneNumber(phoneNumber);
        clientQueryForm.setNhis(null);

        return new HashMap<String, Object>() {{
            put(FORM_BEAN, clientQueryForm);
        }};
    }

    private MRSObservation mockPregnancyObservationWithEDD(Date edd) {
        MRSObservation<Concept> pregnancyObservation = new MRSObservation<Concept>(DateUtil.now().toDate(), Concept.PREGNANCY.getName(), null);
        MRSObservation eddObservation = new MRSObservation<Date>(DateUtil.now().toDate(), Concept.EDD.getName(), edd);
        pregnancyObservation.setDependantObservations(new HashSet<MRSObservation>(asList(eddObservation)));
        return pregnancyObservation;
    }

    private MRSPerson person(String phoneNumber, Date dateOfBirth, int age, String gender, String lastName, String firstName) {
        MRSPerson person = new MRSPerson();
        person.dateOfBirth(dateOfBirth);
        person.firstName(firstName);
        person.lastName(lastName);
        person.age(age);
        person.gender(gender);
        person.attributes(asList(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), phoneNumber)));
        return person;
    }

    private Map<String, Object> createMotechEventWithForm(String facilityId, String motechId, String staffId, String responsePhoneNumber, String clientQueryType) {
        final ClientQueryForm clientQueryForm = clientQueryForm(facilityId, motechId, staffId, responsePhoneNumber, clientQueryType);
        return new HashMap<String, Object>() {{
            put(FORM_BEAN, clientQueryForm);
        }};
    }

    private ClientQueryForm clientQueryForm(String facilityId, String motechId, String staffId, String responsePhoneNumber, String clientQueryType) {
        ClientQueryForm clientQueryForm = new ClientQueryForm();
        clientQueryForm.setFacilityId(facilityId);
        clientQueryForm.setMotechId(motechId);
        clientQueryForm.setStaffId(staffId);
        clientQueryForm.setSender(responsePhoneNumber);
        clientQueryForm.setQueryType(clientQueryType);
        return clientQueryForm;
    }


}
