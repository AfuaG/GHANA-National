package org.motechproject.ghana.national.service;

import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ghana.national.builder.MobileMidwifeEnrollmentBuilder;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.repository.AllMobileMidwifeEnrollments;
import org.motechproject.model.DayOfWeek;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class MobileMidwifeServiceTest {
    @Mock
    private AllMobileMidwifeEnrollments allEnrollments;
    private MobileMidwifeService service;
    @Mock
    private MobileMidwifeCampaign mockMobileMidwifeCampaign;

    public MobileMidwifeServiceTest() {
        initMocks(this);
        service = new MobileMidwifeService();
        setField(service, "allEnrollments", allEnrollments);
        setField(service, "mobileMidwifeCampaign", mockMobileMidwifeCampaign);
    }

    @Test
    public void shouldCreateMobileMidwifeEnrollmentAndCreateScheduleIfNotRegisteredAlready() {
        String patientId = "patientId";
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(true).dayOfWeek(DayOfWeek.Thursday)
                .build();
        when(allEnrollments.findActiveBy(patientId)).thenReturn(null);

        service.register(enrollment);
        verify(allEnrollments).add(enrollment);
        verify(mockMobileMidwifeCampaign).start(enrollment);
    }

    @Test
    public void shouldCreateNewScheduleOnlyIfEnrolledWithConsentYes() {
        MobileMidwifeEnrollment enrollmentWithNoConsent = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId("patienId").staffId("staff13").consent(false).build();
        service.register(enrollmentWithNoConsent);
        verify(allEnrollments).add(enrollmentWithNoConsent);
        verify(mockMobileMidwifeCampaign, never()).start(enrollmentWithNoConsent);
    }

    @Test
    public void shouldStopScheduleOnlyIfEnrolledWithConsentYes() {
        String patientId = "patienId";
        MobileMidwifeEnrollment existingEnrollmentWithNoConsent = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(false).build();
        when(allEnrollments.findActiveBy(patientId)).thenReturn(existingEnrollmentWithNoConsent);

        service.register(new MobileMidwifeEnrollmentBuilder().patientId(patientId).consent(true).build());

        verify(allEnrollments).update(existingEnrollmentWithNoConsent);
        verify(mockMobileMidwifeCampaign, never()).stop(existingEnrollmentWithNoConsent);
    }

    @Test
    public void shouldDeactivateExistingEnrollmentAndCampaign_AndCreateNewEnrollmentIfEnrolledAlready() {
        String patientId = "patientId";
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(true).dayOfWeek(DayOfWeek.Thursday)
                .build();
        MobileMidwifeEnrollment existingEnrollment = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId(patientId).consent(true).build();
        when(allEnrollments.findActiveBy(patientId)).thenReturn(existingEnrollment);
        service = spy(service);

        service.register(enrollment);
        assertTrue(enrollment.getActive());
        verify(service).unregister(existingEnrollment);
        verify(allEnrollments).add(enrollment);
        verify(mockMobileMidwifeCampaign).start(enrollment);
    }
    
    @Test
    public void shouldDeactivateEnrollmentAndClearScheduleOnUnregister() {
        String patientId = "patientId";
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(true).dayOfWeek(DayOfWeek.Thursday)
                .build();
        when(allEnrollments.findActiveBy(patientId)).thenReturn(enrollment);

        service.unregister(enrollment);
        assertFalse(enrollment.getActive());
        verify(allEnrollments).update(enrollment);
        verify(mockMobileMidwifeCampaign).stop(enrollment);
    }

    @Test
    public void shouldFindMobileMidwifeEnrollmentByPatientId() {
        String patientId = "patientId";
        service.findActiveBy(patientId);
        verify(allEnrollments).findActiveBy(patientId);
    }

    @Test
    public void shouldFindLatestMobileMidwifeEnrollmentByPatientId() {
        String patientId = "patientId";
        service.findLatestEnrollment(patientId);
        verify(allEnrollments).findLatestEnrollment(patientId);
    }

}
