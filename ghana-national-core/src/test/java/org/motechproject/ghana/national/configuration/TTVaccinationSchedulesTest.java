package org.motechproject.ghana.national.configuration;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.ghana.national.domain.TTVaccineDosage;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.quartz.SchedulerException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.motechproject.ghana.national.configuration.ScheduleNames.TT_VACCINATION;
import static org.motechproject.scheduletracking.api.domain.WindowName.due;
import static org.motechproject.scheduletracking.api.domain.WindowName.late;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
public class TTVaccinationSchedulesTest extends BaseScheduleTrackingTest {

    @Before
    public void setUp() {
        super.setUp();
        scheduleName = TT_VACCINATION.getName();
    }

    @Test
    public void verifyScheduleCreatedForTT2() throws SchedulerException {
        LocalDate firstDosageDate = mockToday(newDate("01-FEB-2012"));
        enrollmentId = scheduleAlertForTTVaccination(firstDosageDate);
        fulfillCurrentMilestone(firstDosageDate);

        assertTestAlerts(captureAlertsForNextMilestone(enrollmentId), asList(
                alert(due, onDate("22-Feb-2012")),
                alert(late, onDate("29-Feb-2012")),
                alert(late, onDate("7-Mar-2012")),
                alert(late, onDate("14-Mar-2012"))));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("28-Mar-2012", "00:00").toDate())));
        fulfillCurrentMilestone(newDate("22-Mar-2012"));

        assertTestAlerts(captureAlertsForNextMilestone(enrollmentId), asList(
                alert(due, onDate("15-Sep-2012")),
                alert(late, onDate("25-Sep-2012")),
                alert(late, onDate("2-Oct-2012")),
                alert(late, onDate("9-Oct-2012"))));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("23-Oct-2012", "00:00").toDate())));
        fulfillCurrentMilestone(newDate("17-Oct-2012"));

        assertTestAlerts(captureAlertsForNextMilestone(enrollmentId), asList(
                alert(due, onDate("10-Oct-2013")),
                alert(late, onDate("20-Oct-2013")),
                alert(late, onDate("27-Oct-2013")),
                alert(late, onDate("03-Nov-2013"))));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("17-Nov-2013", "00:00").toDate())));
        fulfillCurrentMilestone(newDate("11-Nov-2013"));

        assertTestAlerts(captureAlertsForNextMilestone(enrollmentId), asList(
                alert(due, onDate("04-Nov-2014")),
                alert(late, onDate("14-Nov-2014")),
                alert(late, onDate("21-Nov-2014")),
                alert(late, onDate("28-Nov-2014"))));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("12-Dec-2014", "00:00").toDate())));
    }

    @Test
    public void verifyTTVaccinationScheduleGivenFirstDosageInThePast() throws SchedulerException, ParseException {
        mockToday(newDate("24-FEB-2000"));
        final LocalDate firstDosageDate = newDate("01-FEB-2000");

        enrollmentId = scheduleAlertForTTVaccination(firstDosageDate);
        fulfillCurrentMilestone(firstDosageDate);

        assertAlerts(captureAlertsForNextMilestone(enrollmentId), dates(newDate("29-FEB-2000"), newDate("07-MAR-2000"), newDate("14-MAR-2000")));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("28-Mar-2000", "00:00").toDate())));
    }

    @Test
    public void shouldVerifyTTVaccinationScheduleOnANCRegistration() throws SchedulerException {
        LocalDate ancRegistrationDate = mockToday(newDate("19-Mar-2012"));
        enrollmentId = scheduleAlertForTTVaccination(ancRegistrationDate);

        assertTestAlerts(captureAlertsForNextMilestone(enrollmentId), asList(
                alert(late, onDate("19-Mar-2012")),
                alert(late, onDate("26-Mar-2012")),
                alert(late, onDate("2-Apr-2012"))));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("16-Apr-2012", "00:00").toDate())));
    }


    private String scheduleAlertForTTVaccination(LocalDate firstDosageDate) {
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest(PATIENT_ID, scheduleName, preferredAlertTime, firstDosageDate, null, null, null, null, null);
        return scheduleTrackingService.enroll(enrollmentRequest);
    }
}
