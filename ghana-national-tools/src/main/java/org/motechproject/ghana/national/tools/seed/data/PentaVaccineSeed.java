package org.motechproject.ghana.national.tools.seed.data;

import org.motechproject.ghana.national.configuration.ScheduleNames;
import org.motechproject.ghana.national.repository.AllSchedules;
import org.motechproject.ghana.national.tools.seed.data.domain.UpcomingSchedule;
import org.motechproject.ghana.national.tools.seed.data.source.OldGhanaScheduleSource;
import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PentaVaccineSeed extends ScheduleMigrationSeed {

    @Autowired
    public PentaVaccineSeed(OldGhanaScheduleSource oldGhanaScheduleSource, AllTrackedSchedules allTrackedSchedules, AllSchedules allSchedules) {
        super(allTrackedSchedules, oldGhanaScheduleSource, allSchedules);
    }

    protected List<UpcomingSchedule> getAllUpcomingSchedules() {
        return oldGhanaScheduleSource.getUpcomingPentaSchedules();
    }

    @Override
    public String getScheduleName() {
        return ScheduleNames.CWC_PENTA;
    }

    protected String mapMilestoneName(String milestoneName) {
        return milestoneName;
    }
}
