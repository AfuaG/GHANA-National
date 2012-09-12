package org.motechproject.ghana.national.configuration;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.ghana.national.vo.Pregnancy;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.quartz.SchedulerException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
public class EDDAlertSchedulesTest extends BaseScheduleTrackingTest {
    @Before
    public void setUp() {
        super.setUp();
        scheduleName = ScheduleNames.ANC_DELIVERY.getName();
    }

    @Test
    public void verifyPregnancyScheduleWhenEDDIsVeryFarInFuture() throws SchedulerException, ParseException {
        mockToday(newDate("01-FEB-2012"));
        LocalDate expectedDeliveryDate = newDate("10-NOV-2012");

        enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
        assertAlerts(captureAlertsForNextMilestone(enrollmentId),dates(newDate("03-NOV-2012"), newDate("10-NOV-2012"), newDate("17-NOV-2012"), newDate("24-NOV-2012")));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("08-Dec-2012", "00:00").toDate())));
    }

    @Test
    public void verifyPregnancyScheduleWhenEDDIsUnderOneWeekFromToday() throws SchedulerException, ParseException {
        mockToday(newDate("01-FEB-2012"));
        LocalDate expectedDeliveryDate = newDate("03-FEB-2012");

        enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
        assertAlerts(captureAlertsForNextMilestone(enrollmentId), dates(newDate("03-FEB-2012"), newDate("10-FEB-2012"), newDate("17-FEB-2012")));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("02-Mar-2012", "00:00").toDate())));
    }

    @Test
    public void verifyPregnancyScheduleWhenEDDIsInThePast() throws SchedulerException, ParseException {
        mockToday(newDate("01-FEB-2012"));
        LocalDate expectedDeliveryDate = newDate("23-JAN-2012");

        enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
        assertAlerts(captureAlertsForNextMilestone(enrollmentId), dates(newDate("06-FEB-2012")));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("20-Feb-2012", "00:00").toDate())));
    }

    @Test
    public void verifyPregnancyScheduleWhenEDDIsInOverOneWeekFromToday() throws SchedulerException, ParseException {
        mockToday(newDate("01-FEB-2012"));
        LocalDate expectedDeliveryDate = newDate("12-FEB-2012");
        enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
        assertAlerts(captureAlertsForNextMilestone(enrollmentId), dates(newDate("05-FEB-2012"),newDate("12-FEB-2012"), newDate("19-FEB-2012"), newDate("26-FEB-2012")));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("11-Mar-2012", "00:00").toDate())));
    }

    @Test
    public void verifyPregnancyScheduleWhenEDDIsToday() throws SchedulerException, ParseException {
        mockToday(newDate("01-FEB-2012"));
        LocalDate expectedDeliveryDate = newDate("02-FEB-2012");

        enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
        assertAlerts(captureAlertsForNextMilestone(enrollmentId), dates(newDate("02-FEB-2012"), newDate("09-FEB-2012"), newDate("16-FEB-2012")));
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("01-Mar-2012", "00:00").toDate())));
    }

    @Test
    public void verifyPregnancyScheduleWhenEDDIsInTheFarPast() throws SchedulerException, ParseException {
        mockToday(newDate("01-FEB-2012"));
        LocalDate expectedDeliveryDate = newDate("04-JAN-2012");
        enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);

        assertAlerts(captureAlertsForNextMilestone(enrollmentId), new ArrayList<Date>());
        assertThat(getDefaultmentDate(enrollmentId), is(equalTo(newDateWithTime("01-Feb-2012", "00:00").toDate())));
    }

    private String scheduleAlertForDeliveryNotfication(LocalDate expectedDeliveryDate) {
        final LocalDate conceptionDate = Pregnancy.basedOnDeliveryDate(expectedDeliveryDate).dateOfConception();
        EnrollmentRequest enrollmentRequest = new EnrollmentRequest().setExternalId("123").setScheduleName(scheduleName)
                .setPreferredAlertTime(preferredAlertTime).setReferenceDate(conceptionDate);
        return scheduleTrackingService.enroll(enrollmentRequest);
    }
}
