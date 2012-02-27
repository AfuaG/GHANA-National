package org.motechproject.ghana.national.handler;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.motechproject.ghana.national.vo.Pregnancy;
import org.motechproject.scheduletracking.api.domain.Milestone;
import org.motechproject.scheduletracking.api.domain.MilestoneAlert;
import org.motechproject.scheduletracking.api.events.MilestoneEvent;
import org.motechproject.util.DateUtil;

import static org.joda.time.Period.weeks;
import static org.mockito.Mockito.*;
import static org.motechproject.ghana.national.domain.SmsKeys.*;

public class CareScheduleHandlerTest {

    private CareScheduleHandler careScheduleHandlerSpy;

    @Before
    public void setUp() throws Exception {
        careScheduleHandlerSpy = spy(new CareScheduleHandler());
    }

    @Test
    public void shouldHandlePregnancyAlert() {
        doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());

        LocalDate conceptionDate = DateUtil.newDate(2000, 11, 11);
        final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, null, null, conceptionDate);

        careScheduleHandlerSpy.handlePregnancyAlert(milestoneEvent);

        verify(careScheduleHandlerSpy).sendSMSToFacility(PREGNANCY_ALERT_SMS_KEY, milestoneEvent, Pregnancy.basedOnConceptionDate(conceptionDate).dateOfDelivery());
    }

    @Test
    public void shouldHandleTTVaccinationAlert() {
        doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());

        LocalDate referenceDate = DateUtil.newDate(2012, 2, 1);
        Milestone milestone = new Milestone("M1", weeks(0), weeks(3), weeks(6), weeks(7));
        final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, MilestoneAlert.fromMilestone(milestone, referenceDate), null, referenceDate);

        careScheduleHandlerSpy.handleTTVaccinationAlert(milestoneEvent);

        verify(careScheduleHandlerSpy).sendSMSToFacility(TT_VACCINATION_SMS_KEY, milestoneEvent, DateUtil.newDate(2012, 2, 29));
    }

    @Test
    public void shouldHandleBCGAlert() {
        doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());

        LocalDate referenceDate = DateUtil.newDate(2012, 2, 1);
        Milestone milestone = new Milestone("M1", weeks(0), weeks(3), weeks(6), weeks(7));
        final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, MilestoneAlert.fromMilestone(milestone, referenceDate), null, referenceDate);

        careScheduleHandlerSpy.handleBCGAlert(milestoneEvent);

        verify(careScheduleHandlerSpy).sendSMSToFacility(BCG_SMS_KEY, milestoneEvent, DateUtil.newDate(2012, 2, 29));
    }

    @Test
    public void shouldHandleAncVisitAlert() {
        doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());

        LocalDate referenceDate = DateUtil.newDate(2012, 2, 1);
        Milestone milestone = new Milestone("M1", weeks(0), weeks(0), weeks(1), weeks(3));
        final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, MilestoneAlert.fromMilestone(milestone, referenceDate), null, referenceDate);

        careScheduleHandlerSpy.handleAncVisitAlert(milestoneEvent);

        verify(careScheduleHandlerSpy).sendSMSToFacility(ANC_VISIT_SMS_KEY, milestoneEvent, DateUtil.newDate(2012, 2, 8));
    }

    @Test
    public void handleIPTpVaccinationAlert() {
        doNothing().when(careScheduleHandlerSpy).sendSMSToFacility(Matchers.<String>any(), Matchers.<MilestoneEvent>any(), Matchers.<LocalDate>any());

        LocalDate conceptionDate = DateUtil.newDate(2011, 12, 17);
        Milestone milestone = new Milestone("M1", weeks(12), weeks(2), weeks(3), weeks(0));
        final MilestoneEvent milestoneEvent = new MilestoneEvent(null, null, MilestoneAlert.fromMilestone(milestone, conceptionDate), null, conceptionDate);

        careScheduleHandlerSpy.handleIPTpVaccinationAlert(milestoneEvent);

        verify(careScheduleHandlerSpy).sendSMSToFacility(IPTp_VACCINATION_SMS_KEY, milestoneEvent, DateUtil.newDate(2012, 3, 31));
    }
}
