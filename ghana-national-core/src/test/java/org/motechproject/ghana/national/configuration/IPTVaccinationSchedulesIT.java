package org.motechproject.ghana.national.configuration;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.motechproject.model.Time;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
import org.motechproject.util.DateUtil;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
public class IPTVaccinationSchedulesIT extends BaseScheduleTrackingIT {

    private Time preferredAlertTime;

    @Before
    public void setUp() {
        super.setUp();
        preferredAlertTime = new Time(10, 10);
    }

    @Test
    @Ignore
    public void verifyPregnancyIPT1Schedule() throws SchedulerException {

        LocalDate today = DateUtil.newDate(2012, 1, 1);
        mockCurrentDate(today);
        final LocalDate conceptionDate = today.minusWeeks(11);

        EnrollmentRequest enrollmentRequest = new EnrollmentRequest("patient_id", CareScheduleNames.ANC_IPT_VACCINE, preferredAlertTime, conceptionDate);
        String enrollmentId = scheduleTrackingService.enroll(enrollmentRequest);

        List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
        assertAlerts(alerts, new HashSet<Date>() {{
            add(onDate(conceptionDate, 12, preferredAlertTime));
            add(onDate(conceptionDate, 13, preferredAlertTime));
            add(onDate(conceptionDate, 14, preferredAlertTime));
            add(onDate(conceptionDate, 15, preferredAlertTime));
        }});
    }

}
