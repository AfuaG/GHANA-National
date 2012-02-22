package org.motechproject.ghana.national.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.scheduletracking.api.service.EnrollmentResponse;
import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
import org.motechproject.util.DateUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.configuration.ScheduleNames.TT_VACCINATION_VISIT;
import static org.motechproject.ghana.national.domain.TTVaccineDosage.TT1;

public class BaseScheduleServiceTest {
    @Mock
    private EncounterService encounterService;
    @Mock
    private ScheduleTrackingService scheduleTrackingService;

    private CareVisitService careVisitService;

    private final String staffId = "staff id";
    private final String facilityId = "facility id";
    private final Patient patient = new Patient(new MRSPatient("patient id", null, null));
    private final LocalDate vaccinationDate = DateUtil.newDate(2000, 2, 1);


    @Before
    public void setUp() {
        initMocks(this);
        careVisitService = new CareVisitService(encounterService, scheduleTrackingService);
    }

    @Test
    public void shouldEnrollNewPatientsAndFulfillCurrentMilestone() {

        when(scheduleTrackingService.getEnrollment(patient.getMRSPatientId(), TT_VACCINATION_VISIT)).thenReturn(null);
        careVisitService.receivedTT(TT1, patient, staffId, facilityId, vaccinationDate);

        ArgumentCaptor<EnrollmentRequest> enrollmentRequestArgCaptor = ArgumentCaptor.forClass(EnrollmentRequest.class);
        verify(scheduleTrackingService).enroll(enrollmentRequestArgCaptor.capture());

        final EnrollmentRequest enrollmentRequest = enrollmentRequestArgCaptor.getValue();
        assertThat(enrollmentRequest.getScheduleName(), is(equalTo(TT_VACCINATION_VISIT)));
        assertThat(enrollmentRequest.getStartingMilestoneName(), is(equalTo(TT1.name())));
        assertThat(enrollmentRequest.getReferenceDate(), is(equalTo(vaccinationDate)));

        verify(scheduleTrackingService).fulfillCurrentMilestone(patient.getMRSPatientId(), TT_VACCINATION_VISIT);
    }
    
    @Test
    public void shouldFulfillCurrentMilestoneForUsersHowHaveAlreadyEnrolledIntoTheProgram(){
        EnrollmentResponse enrollmentResponse = mock(EnrollmentResponse.class);
        when(scheduleTrackingService.getEnrollment(patient.getMRSPatientId(), TT_VACCINATION_VISIT)).thenReturn(enrollmentResponse);

        careVisitService.receivedTT(TT1, patient, staffId, facilityId, vaccinationDate);

        verify(scheduleTrackingService, never()).enroll(Matchers.<EnrollmentRequest>any());
        verify(scheduleTrackingService).fulfillCurrentMilestone(patient.getMRSPatientId(), TT_VACCINATION_VISIT);
    }
}
