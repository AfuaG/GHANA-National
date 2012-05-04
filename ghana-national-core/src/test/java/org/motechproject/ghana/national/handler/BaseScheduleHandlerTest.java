package org.motechproject.ghana.national.handler;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.appointments.api.EventKeys;
import org.motechproject.ghana.national.domain.*;
import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.messagegateway.domain.MessageRecipientType;
import org.motechproject.ghana.national.repository.AllMobileMidwifeEnrollments;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.repository.SMSGateway;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.*;
import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.scheduletracking.api.domain.Milestone;
import org.motechproject.scheduletracking.api.domain.MilestoneAlert;
import org.motechproject.scheduletracking.api.domain.WindowName;
import org.motechproject.scheduletracking.api.events.MilestoneEvent;
import org.motechproject.util.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.configuration.TextMessageTemplateVariables.*;
import static org.motechproject.ghana.national.domain.SMSTemplateTest.assertContainsTemplateValues;
import static org.motechproject.ghana.national.domain.SmsTemplateKeys.*;

public class BaseScheduleHandlerTest {

    @Mock
    private PatientService mockPatientService;
    @Mock
    private FacilityService mockFacilityService;
    @Mock
    private AllObservations mockAllObservations;
    @Mock
    private SMSGateway mockSMSGateway;
    @Mock
    private AllMobileMidwifeEnrollments mockAllMobileMidwifeEnrollments;

    private CareScheduleAlerts careScheduleHandler;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        careScheduleHandler = new CareScheduleAlerts(mockPatientService, mockFacilityService, mockSMSGateway, mockAllObservations, mockAllMobileMidwifeEnrollments);
    }

    @Test
    public void shouldSendAggregativeSMSToFacilityForAnAppointment() {
        String ancVisitKey = "ancVisitKey";
        final String facilityId = "facilityid";
        final String patientId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String visitName = "ancVisit";

        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(EventKeys.EXTERNAL_ID_KEY, patientId);
        parameters.put(EventKeys.VISIT_NAME, visitName);
        parameters.put(MotechSchedulerService.JOB_ID_KEY, visitName + "3");

        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(1999, 3, 3).toDate());
        when(mockPatientService.getPatientByMotechId(patientId)).thenReturn(new Patient(new MRSPatient(patientId, person, new MRSFacility(facilityId))));

        final String phoneNumber = "phoneNumber";
        String additionalPhone = "addPhone";
        Facility facility = new Facility().phoneNumber(phoneNumber).additionalPhoneNumber1(additionalPhone);
        when(mockFacilityService.getFacility(facilityId)).thenReturn(facility);

        careScheduleHandler.sendAggregatedSMSToFacilityForAnAppointment(ancVisitKey, new MotechEvent("subject", parameters));

        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockSMSGateway, times(2)).dispatchSMSToAggregator(eq(ancVisitKey), templateValuesArgCaptor.capture(), captor.capture(), eq(new AggregationMessageIdentifier(patientId, visitName).getIdentifier()),eq(MessageRecipientType.FACILITY));

        List<String> allPhoneNumbers = captor.getAllValues();
        assertEquals(2, allPhoneNumbers.size());
        assertEquals(facility.getPhoneNumbers(), allPhoneNumbers);

        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientId);
            put(WINDOW, "Overdue");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastname);
            put(MILESTONE_NAME, visitName);
        }}, templateValuesArgCaptor.getValue());
    }

    @Test
    public void shouldNotSendSMSToPatientIfThePatientIsNotInDueOrLateWindow() {
        final String patientId = "patientid";
        final String scheduleName = "edd";
        final String milestoneName="milestone1";
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone(milestoneName, Period.days(1), Period.days(1), Period.days(1), Period.days(1)), DateTime.now());
        when(mockPatientService.patientByOpenmrsId(anyString())).thenReturn(new Patient(new MRSPatient("121", new MRSPerson(), new MRSFacility("123"))));
        careScheduleHandler.sendAggregatedSMSToPatient(PATIENT_IPT, new MilestoneEvent(patientId, scheduleName, milestoneAlert, WindowName.earliest.name(), null));
        verify(mockSMSGateway, never()).dispatchSMSToAggregator(anyString(), anyMap(), anyString(), anyString(), org.mockito.Matchers.<MessageRecipientType>any());

        careScheduleHandler.sendAggregatedSMSToPatient(PATIENT_IPT, new MilestoneEvent(patientId, scheduleName, milestoneAlert, WindowName.max.name(), null));
        verify(mockSMSGateway, never()).dispatchSMSToAggregator(anyString(), anyMap(), anyString(), anyString(), org.mockito.Matchers.<MessageRecipientType>any());
    }

    @Test
    public void shouldSendAggregatedSMSToPatientForANCAppointmentUsingPatientContactNumberIfNotRegisteredToMM() {
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastName = "lastname";
        final String serialNumber = "serialNumber";
        String patientPhoneNumber = "123456";
        final String visitName = "ancVisit";
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(EventKeys.EXTERNAL_ID_KEY, patientMotechId);
        parameters.put(EventKeys.VISIT_NAME, visitName);
        parameters.put(MotechSchedulerService.JOB_ID_KEY, visitName + "3");

        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastName).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate()).addAttribute(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), patientPhoneNumber));
        when(mockPatientService.getPatientByMotechId(patientMotechId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility("123"))));
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));
        careScheduleHandler.sendAggregatedSMSToPatientForAppointment(PATIENT_IPT, new MotechEvent("subject", parameters));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockSMSGateway).dispatchSMSToAggregator(eq(PATIENT_IPT + PATIENT_LATE_SMS_KEY), templateValuesArgCaptor.capture(), captor.capture(), eq(new AggregationMessageIdentifier(patientMotechId, visitName).getIdentifier()),eq( MessageRecipientType.PATIENT));

        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientMotechId);
            put(WINDOW, "Overdue");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastName);
            put(MILESTONE_NAME, visitName);
        }}, templateValuesArgCaptor.getValue());

        assertThat(captor.getValue(), is(patientPhoneNumber));
    }

    @Test
    public void shouldNotSendSMSForAppointmentIfNoPhoneNumberIsFound() {
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String serialNumber = "serialNumber";
        final String visitName = "ancVisit";
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(EventKeys.EXTERNAL_ID_KEY, patientMotechId);
        parameters.put(EventKeys.VISIT_NAME, visitName);
        parameters.put(MotechSchedulerService.JOB_ID_KEY, visitName + "3");
        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate());
        when(mockPatientService.getPatientByMotechId(patientMotechId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility("123"))));
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollment().setMedium(Medium.SMS);
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientMotechId)).thenReturn(enrollment);

        careScheduleHandler.sendAggregatedSMSToPatientForAppointment(PATIENT_IPT, new MotechEvent("subject", parameters));

        verify(mockSMSGateway, never()).dispatchSMSToAggregator(anyString(), anyMap(), anyString(), anyString(),org.mockito.Matchers.<MessageRecipientType>any());
    }

    @Test
    public void shouldSendAggregatedSMSToPatientForAppointmentsMobileMidWifeRegisteredPhoneNumber() {
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String serialNumber = "serialNumber";
        String mmRegisteredPhoneNumber = "4353543";
        String patientPhoneNumber = "123456";
        final String visitName = "ancVisit";
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        parameters.put(EventKeys.EXTERNAL_ID_KEY, patientMotechId);
        parameters.put(EventKeys.VISIT_NAME, visitName);
        parameters.put(MotechSchedulerService.JOB_ID_KEY, visitName + "3");
        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate()).addAttribute(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), patientPhoneNumber));
        when(mockPatientService.getPatientByMotechId(patientMotechId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility("123"))));
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollment().setMedium(Medium.SMS).setPhoneNumber(mmRegisteredPhoneNumber);
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientMotechId)).thenReturn(enrollment);

        careScheduleHandler.sendAggregatedSMSToPatientForAppointment(PATIENT_IPT, new MotechEvent("subject", parameters));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockSMSGateway).dispatchSMSToAggregator(eq(PATIENT_IPT + PATIENT_LATE_SMS_KEY), templateValuesArgCaptor.capture(), captor.capture(), eq(new AggregationMessageIdentifier(patientMotechId, visitName).getIdentifier()),eq( MessageRecipientType.PATIENT));

        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientMotechId);
            put(WINDOW, "Overdue");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastname);
            put(SERIAL_NUMBER, serialNumber);
        }}, templateValuesArgCaptor.getValue());

        assertThat(captor.getValue(), is(mmRegisteredPhoneNumber));
    }


    @Test
    public void shouldSendAggregatedSMSToPatientUsingPatientContactNumberIfNotRegisteredToMM() {
        final String patientId = "patientid";
        final String windowName = WindowName.due.name();
        final String scheduleName = "edd";
        final String milestoneName="milestone1";
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String serialNumber = "serialNumber";
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone(milestoneName, Period.days(1), Period.days(1), Period.days(1), Period.days(1)), DateTime.now());
        MilestoneEvent milestoneEvent = new MilestoneEvent(patientId, scheduleName, milestoneAlert, windowName, null);
        String patientPhoneNumber = "123456";
        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate()).addAttribute(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), patientPhoneNumber));
        when(mockPatientService.patientByOpenmrsId(patientId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility("123"))));
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));

        careScheduleHandler.sendAggregatedSMSToPatient(PATIENT_IPT, milestoneEvent);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockSMSGateway).dispatchSMSToAggregator(eq(PATIENT_IPT + PATIENT_DUE_SMS_KEY), templateValuesArgCaptor.capture(), captor.capture(), eq(new AggregationMessageIdentifier(patientId, scheduleName).getIdentifier()), eq(MessageRecipientType.PATIENT));

        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientMotechId);
            put(WINDOW, "Due");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastname);
            put(MILESTONE_NAME, milestoneName);
            put(SERIAL_NUMBER, serialNumber);
        }}, templateValuesArgCaptor.getValue());

        assertThat(captor.getValue(), is(patientPhoneNumber));
    }

    @Test
    public void shouldNotSendSMSIfNoPhoneNumberIsFound() {
        final String patientId = "patientid";
        final String windowName = WindowName.late.name();
        final String scheduleName = "edd";
        final String milestoneName="milestone1";
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String serialNumber = "serialNumber";
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone(milestoneName, Period.days(1), Period.days(1), Period.days(1), Period.days(1)), DateTime.now());
        MilestoneEvent milestoneEvent = new MilestoneEvent(patientId, scheduleName, milestoneAlert, windowName, null);
        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate());
        when(mockPatientService.patientByOpenmrsId(patientId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility("123"))));
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollment().setMedium(Medium.SMS);
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientMotechId)).thenReturn(enrollment);

        careScheduleHandler.sendAggregatedSMSToPatient(PATIENT_IPT, milestoneEvent);

        verify(mockSMSGateway, never()).dispatchSMSToAggregator(anyString(), anyMap(), anyString(), anyString(),org.mockito.Matchers.<MessageRecipientType>any());
    }

    @Test
    public void shouldSendAggregatedSMSToPatientsMobileMidWifeRegisteredPhoneNumber() {
        final String patientId = "patientid";
        final String windowName = WindowName.late.name();
        final String scheduleName = "edd";
        final String milestoneName="milestone1";
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String serialNumber = "serialNumber";
        String mmRegisteredPhoneNumber = "4353543";
        String patientPhoneNumber = "123456";
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone(milestoneName, Period.days(1), Period.days(1), Period.days(1), Period.days(1)), DateTime.now());
        MilestoneEvent milestoneEvent = new MilestoneEvent(patientId, scheduleName, milestoneAlert, windowName, null);
        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate()).addAttribute(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), patientPhoneNumber));
        when(mockPatientService.patientByOpenmrsId(patientId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility("123"))));
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollment().setMedium(Medium.SMS).setPhoneNumber(mmRegisteredPhoneNumber);
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientMotechId)).thenReturn(enrollment);

        careScheduleHandler.sendAggregatedSMSToPatient(PATIENT_IPT, milestoneEvent);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockSMSGateway).dispatchSMSToAggregator(eq(PATIENT_IPT + PATIENT_LATE_SMS_KEY), templateValuesArgCaptor.capture(), captor.capture(), eq(new AggregationMessageIdentifier(patientId, scheduleName).getIdentifier()), eq(MessageRecipientType.PATIENT));

        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientMotechId);
            put(WINDOW, "Overdue");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastname);
            put(MILESTONE_NAME, milestoneName);
            put(SERIAL_NUMBER, serialNumber);
        }}, templateValuesArgCaptor.getValue());

        assertThat(captor.getValue(), is(mmRegisteredPhoneNumber));
    }

    @Test
    public void shouldSendAggregativeSMSToFacility() {

        final String patientId = "patientid";
        final String facilityId = "facilityid";
        final String patientMotechId = "patientmotechid";
        final String firstName = "firstName";
        final String lastname = "lastname";
        final String serialNumber = "serialNumber";
        final String windowName = WindowName.due.name();
        final String scheduleName = "edd";
        final String milestoneName="milestone1";

        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate());
        when(mockPatientService.patientByOpenmrsId(patientId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility(facilityId))));

        final String phoneNumber = "phoneNumber";
        String additionalPhoneNumber = "addPhone";
        Facility facility = new Facility().phoneNumber(phoneNumber).additionalPhoneNumber1(additionalPhoneNumber);
        when(mockFacilityService.getFacility(facilityId)).thenReturn(facility);
        when(mockAllObservations.findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName())).thenReturn(new MRSObservation<String>(new Date(), Concept.SERIAL_NUMBER.getName(), serialNumber));
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone(milestoneName, Period.days(1), Period.days(1), Period.days(1), Period.days(1)), DateTime.now());
        MilestoneEvent milestoneEvent = new MilestoneEvent(patientId, scheduleName, milestoneAlert, windowName, null);
        careScheduleHandler.sendAggregatedSMSToFacility(PREGNANCY_ALERT_SMS_KEY, milestoneEvent);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        verify(mockSMSGateway, times(2)).dispatchSMSToAggregator(eq(PREGNANCY_ALERT_SMS_KEY), templateValuesArgCaptor.capture(), captor.capture(), eq(new AggregationMessageIdentifier(patientId, scheduleName).getIdentifier()), eq(MessageRecipientType.FACILITY));
        verify(mockAllObservations).findLatestObservation(patientMotechId, Concept.SERIAL_NUMBER.getName());
        List<String> allPhoneNumbers = captor.getAllValues();
        assertEquals(2, allPhoneNumbers.size());
        assertEquals(facility.getPhoneNumbers(), allPhoneNumbers);
        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientMotechId);
            put(WINDOW, "Due");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastname);
            put(MILESTONE_NAME, milestoneName);
            put(SERIAL_NUMBER, serialNumber);
        }}, templateValuesArgCaptor.getValue());
    }

    @Test
    public void shouldSendInstantSMSToFacility() {

        final String patientId = "patientid11";
        final String facilityId = "facilityid22";
        final String patientMotechId = "patientmotechid33";
        final String firstName = "firstName44";
        final String lastname = "lastname55";
        final String windowName = WindowName.late.name();
        final String scheduleName = "some schedule";

        MRSPerson person = new MRSPerson().firstName(firstName).lastName(lastname).dateOfBirth(DateUtil.newDate(2000, 1, 1).toDate());
        when(mockPatientService.patientByOpenmrsId(patientId)).thenReturn(new Patient(new MRSPatient(patientMotechId, person, new MRSFacility(facilityId))));

        final String phoneNumber = "phoneNumber";
        String additionalPhoneNumber1 = "additionalPhone";
        Facility facility = new Facility().phoneNumber(phoneNumber).additionalPhoneNumber1(additionalPhoneNumber1);
        when(mockFacilityService.getFacility(facilityId)).thenReturn(facility);

        final String milestoneName="milestone1";
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone(milestoneName, Period.days(1), Period.days(1), Period.days(1), Period.days(1)), DateTime.now());

        MilestoneEvent milestoneEvent = new MilestoneEvent(patientId, scheduleName, milestoneAlert, windowName, null);

        careScheduleHandler.sendInstantSMSToFacility(PNC_CHILD_SMS_KEY, milestoneEvent);

        ArgumentCaptor<Map> templateValuesArgCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(mockSMSGateway, times(2)).dispatchSMS(eq(PNC_CHILD_SMS_KEY), templateValuesArgCaptor.capture(), captor.capture());

        List<String> allPhoneNumbers = captor.getAllValues();
        assertEquals(2, allPhoneNumbers.size());
        assertEquals(facility.getPhoneNumbers(), allPhoneNumbers);
        assertContainsTemplateValues(new HashMap<String, String>() {{
            put(MOTECH_ID, patientMotechId);
            put(WINDOW, "Overdue");
            put(FIRST_NAME, firstName);
            put(LAST_NAME, lastname);
            put(MILESTONE_NAME, milestoneName);
        }}, templateValuesArgCaptor.getValue());
    }
}
