package org.motechproject.ghana.national.handler;

import org.motechproject.ghana.national.exception.EventHandlerException;
import org.motechproject.scheduler.domain.MotechEvent;
import org.motechproject.scheduletracking.api.events.MilestoneEvent;
import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
import org.motechproject.server.event.annotations.MotechListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.motechproject.ghana.national.configuration.ScheduleNames.ANC_DELIVERY;
import static org.motechproject.ghana.national.configuration.ScheduleNames.ANC_IPT_VACCINE;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_BCG;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_IPT_VACCINE;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_MEASLES_VACCINE;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_OPV_0;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_OPV_OTHERS;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_PENTA;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_ROTAVIRUS;
import static org.motechproject.ghana.national.configuration.ScheduleNames.CWC_YELLOW_FEVER;
import static org.motechproject.ghana.national.configuration.ScheduleNames.PNC_CHILD_1;
import static org.motechproject.ghana.national.configuration.ScheduleNames.PNC_CHILD_2;
import static org.motechproject.ghana.national.configuration.ScheduleNames.PNC_CHILD_3;
import static org.motechproject.ghana.national.configuration.ScheduleNames.PNC_MOTHER_1;
import static org.motechproject.ghana.national.configuration.ScheduleNames.PNC_MOTHER_2;
import static org.motechproject.ghana.national.configuration.ScheduleNames.PNC_MOTHER_3;
import static org.motechproject.ghana.national.configuration.ScheduleNames.TT_VACCINATION;

@Component
public class ScheduleHandler {
    private Logger logger = LoggerFactory.getLogger(ScheduleHandler.class);

    @Autowired
    private CareScheduleHandler careScheduleHandler;

    @MotechListener(subjects = {EventSubjects.MILESTONE_ALERT})
    public void handleAlert(MotechEvent motechEvent) {
        logger.info("Received milestone event: " + motechEvent);
        try {
            MilestoneEvent milestoneEvent = new MilestoneEvent(motechEvent);
            if (milestoneEvent.getScheduleName().equals(ANC_DELIVERY.getName()))
                careScheduleHandler.handlePregnancyAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(TT_VACCINATION.getName()))
                careScheduleHandler.handleTTVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(ANC_IPT_VACCINE.getName()))
                careScheduleHandler.handleIPTpVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_IPT_VACCINE.getName()))
                careScheduleHandler.handleIPTiVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_BCG.getName()))
                careScheduleHandler.handleBCGAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_MEASLES_VACCINE.getName()))
                careScheduleHandler.handleMeaslesVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_PENTA.getName()))
                careScheduleHandler.handlePentaVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_ROTAVIRUS.getName()))
                careScheduleHandler.handleRotavirusVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_YELLOW_FEVER.getName()))
                careScheduleHandler.handleYellowFeverVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_OPV_0.getName()))
                careScheduleHandler.handleOpvVaccinationAlert(milestoneEvent);
            else if (milestoneEvent.getScheduleName().equals(CWC_OPV_OTHERS.getName()))
                careScheduleHandler.handleOpvVaccinationAlert(milestoneEvent);
            else if (isPNCMotherSchedule(milestoneEvent.getScheduleName()))
                careScheduleHandler.handlePNCMotherAlert(milestoneEvent);
            else if (isPNCChildSchedule(milestoneEvent.getScheduleName()))
                careScheduleHandler.handlePNCChildAlert(milestoneEvent);
        } catch (Exception e) {
            logger.error("<Alert Exception>: Encountered error while sending alert: ", e);
            throw new EventHandlerException(motechEvent, e);
        }
    }

    private boolean isPNCChildSchedule(String scheduleName) {
        return scheduleName.equals(PNC_CHILD_1.getName()) || scheduleName.equals(PNC_CHILD_2.getName()) || scheduleName.equals(PNC_CHILD_3.getName());
    }

    private boolean isPNCMotherSchedule(String scheduleName) {
        return scheduleName.equals(PNC_MOTHER_1.getName()) || scheduleName.equals(PNC_MOTHER_2.getName()) || scheduleName.equals(PNC_MOTHER_3.getName());
    }
}
