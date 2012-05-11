package org.motechproject.ghana.national.service;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.ghana.national.builder.MobileMidwifeEnrollmentBuilder;
import org.motechproject.ghana.national.domain.mobilemidwife.MessageStartWeek;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.domain.mobilemidwife.PhoneOwnership;
import org.motechproject.ghana.national.domain.mobilemidwife.ServiceType;
import org.motechproject.ghana.national.repository.AllCampaigns;
import org.motechproject.ghana.national.repository.AllMobileMidwifeEnrollments;
import org.motechproject.model.DayOfWeek;
import org.motechproject.server.messagecampaign.contract.CampaignRequest;
import org.motechproject.util.DateTimeSourceUtil;
import org.motechproject.util.datetime.DateTimeSource;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.util.DateUtil.newDateTime;
import static org.motechproject.util.DateUtil.now;

public class MobileMidwifeServiceTest {
    private MobileMidwifeService service;
    @Mock
    private AllMobileMidwifeEnrollments mockAllMobileMidwifeEnrollments;
    @Mock
    private AllCampaigns mockAllCampaigns;

    public MobileMidwifeServiceTest() {
        initMocks(this);
        service = new MobileMidwifeService(mockAllMobileMidwifeEnrollments, mockAllCampaigns);
    }

    @Test
    public void shouldCreateMobileMidwifeEnrollmentAndCreateScheduleIfNotRegisteredAlready() {
        String patientId = "patientId";
        mockNow(now());
        final DateTime enrollmentDateTime = newDateTime(2012, 2, 3, 4, 3, 2);
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().serviceType(ServiceType.CHILD_CARE).facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(true).dayOfWeek(DayOfWeek.Thursday).phoneOwnership(PhoneOwnership.HOUSEHOLD)
                .enrollmentDateTime(enrollmentDateTime).messageStartWeek("52")
                .build();
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientId)).thenReturn(null);
        when(mockAllCampaigns.nearestCycleDate(enrollment)).thenReturn(enrollmentDateTime);

        service.register(enrollment);
        assertThat(enrollment.getEnrollmentDateTime(), is(enrollmentDateTime));

        verifyCreateNewEnrollment(enrollment);
        ArgumentCaptor<CampaignRequest> campaignRequestCaptor = ArgumentCaptor.forClass(CampaignRequest.class);
        verify(mockAllCampaigns).start(campaignRequestCaptor.capture());

        assertCampaignRequestWith(enrollment, campaignRequestCaptor.getValue(), enrollmentDateTime.toLocalDate());
    }

    private void assertCampaignRequestWith(MobileMidwifeEnrollment enrollment, CampaignRequest actualRequest, LocalDate expectedScheduleStartDate) {
        assertThat(actualRequest.externalId(), is(enrollment.getPatientId()));
        assertThat(actualRequest.startOffset(), is(MessageStartWeek.findBy(enrollment.getMessageStartWeek()).getWeek()));
        assertThat(actualRequest.campaignName(), is(enrollment.getServiceType().name()));
        assertThat(actualRequest.referenceDate(), is(expectedScheduleStartDate));
        assertNull(actualRequest.reminderTime());
    }

    private void mockNow(final DateTime now) {
        DateTimeSourceUtil.SourceInstance = new DateTimeSource() {
            @Override
            public DateTimeZone timeZone() {
                return DateTimeZone.getDefault();
            }

            @Override
            public DateTime now() {
                return now;
            }

            @Override
            public LocalDate today() {
                return now.toLocalDate();
            }
        };
    }

    private void verifyCreateNewEnrollment(MobileMidwifeEnrollment enrollment) {
        verify(mockAllCampaigns).nearestCycleDate(enrollment);
        verify(mockAllMobileMidwifeEnrollments).add(enrollment);
    }

    @Test
    public void shouldCreateNewScheduleOnlyIfEnrolledWithConsentYes() {
        MobileMidwifeEnrollment enrollmentWithNoConsent = new MobileMidwifeEnrollmentBuilder().serviceType(ServiceType.PREGNANCY).facilityId("facility12").
                patientId("patienId").staffId("staff13").consent(false).messageStartWeek("6").phoneOwnership(PhoneOwnership.PERSONAL).build();
        service.register(enrollmentWithNoConsent);
        verify(mockAllMobileMidwifeEnrollments).add(enrollmentWithNoConsent);
        verify(mockAllCampaigns, never()).start(enrollmentWithNoConsent.createCampaignRequest(Matchers.<LocalDate>any()));
    }

    @Test
    public void shouldStopScheduleOnlyIfEnrolledWithConsentYes() {
        String patientId = "patienId";
        MobileMidwifeEnrollment existingEnrollmentWithNoConsent = new MobileMidwifeEnrollmentBuilder().serviceType(ServiceType.PREGNANCY)
                .facilityId("facility12").patientId(patientId).staffId("staff13").consent(false).phoneOwnership(PhoneOwnership.PERSONAL)
                .messageStartWeek("9").build();
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientId)).thenReturn(existingEnrollmentWithNoConsent);

        MobileMidwifeEnrollment newEnrollment = new MobileMidwifeEnrollmentBuilder().serviceType(ServiceType.PREGNANCY).patientId(patientId)
                .messageStartWeek("9").phoneOwnership(PhoneOwnership.HOUSEHOLD).consent(true).build();
        when(mockAllCampaigns.nearestCycleDate(newEnrollment)).thenReturn(newEnrollment.getEnrollmentDateTime());
        service.register(newEnrollment);

        verify(mockAllMobileMidwifeEnrollments).update(existingEnrollmentWithNoConsent);
        verify(mockAllCampaigns, never()).stop(existingEnrollmentWithNoConsent.stopCampaignRequest());
    }

    @Test
    public void shouldDeactivateExistingEnrollmentAndCampaign_AndCreateNewEnrollmentIfEnrolledAlready() {
        String patientId = "patientId";
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().serviceType(ServiceType.PREGNANCY).facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(true).dayOfWeek(DayOfWeek.Thursday).phoneOwnership(PhoneOwnership.PERSONAL)
                .messageStartWeek("6").build();
        MobileMidwifeEnrollment existingEnrollment = new MobileMidwifeEnrollmentBuilder().serviceType(ServiceType.PREGNANCY).facilityId("facility12").
                messageStartWeek("6").patientId(patientId).consent(true).phoneOwnership(PhoneOwnership.HOUSEHOLD).build();
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientId)).thenReturn(existingEnrollment);
        when(mockAllCampaigns.nearestCycleDate(enrollment)).thenReturn(enrollment.getEnrollmentDateTime());

        service = spy(service);

        service.register(enrollment);
        assertTrue(enrollment.getActive());
        verify(service).unRegister(patientId);
        verifyCreateNewEnrollment(enrollment);
        verify(mockAllCampaigns).start(enrollment.createCampaignRequest(Matchers.<LocalDate>any()));
    }

    @Test
    public void shouldDeactivateEnrollmentAndClearScheduleOnUnregister() {
        String patientId = "patientId";
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().facilityId("facility12").
                patientId(patientId).staffId("staff13").consent(true).dayOfWeek(DayOfWeek.Thursday).phoneOwnership(PhoneOwnership.PERSONAL)
                .messageStartWeek("6").serviceType(ServiceType.PREGNANCY).build();
        when(mockAllMobileMidwifeEnrollments.findActiveBy(patientId)).thenReturn(enrollment);

        service.unRegister(patientId);
        assertFalse(enrollment.getActive());
        verify(mockAllMobileMidwifeEnrollments).update(enrollment);
        ArgumentCaptor<CampaignRequest> campaignRequestCaptor = ArgumentCaptor.forClass(CampaignRequest.class);
        verify(mockAllCampaigns).stop(campaignRequestCaptor.capture());
        assertThat(campaignRequestCaptor.getValue().externalId(), is(patientId));
        assertThat(campaignRequestCaptor.getValue().campaignName(), is(ServiceType.PREGNANCY.name()));
    }

    @Test
    public void shouldFindMobileMidwifeEnrollmentByPatientId() {
        String patientId = "patientId";
        service.findActiveBy(patientId);
        verify(mockAllMobileMidwifeEnrollments).findActiveBy(patientId);
    }

    @Test
    public void shouldFindLatestMobileMidwifeEnrollmentByPatientId() {
        String patientId = "patientId";
        service.findLatestEnrollment(patientId);
        verify(mockAllMobileMidwifeEnrollments).findLatestEnrollment(patientId);
    }

    @Test
    public void shouldCreateEnrollmentAndNotCreateScheduleIfUsersPhoneOwnership_IsPUBLIC() {
        MobileMidwifeEnrollment enrollment = new MobileMidwifeEnrollmentBuilder().consent(true).phoneOwnership(PhoneOwnership.PUBLIC).build();

        service.register(enrollment);
        verify(mockAllMobileMidwifeEnrollments).add(enrollment);
        verify(mockAllCampaigns, never()).nearestCycleDate(enrollment);
        verify(mockAllCampaigns, never()).start(Matchers.<CampaignRequest>any());
    }

}
