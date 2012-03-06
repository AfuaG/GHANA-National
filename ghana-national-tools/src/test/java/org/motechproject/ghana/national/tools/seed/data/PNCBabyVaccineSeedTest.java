package org.motechproject.ghana.national.tools.seed.data;

import org.junit.Before;
import org.junit.Test;
import org.motechproject.ghana.national.configuration.ScheduleNames;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PNCBabyVaccineSeedTest {
    private PNCBabyVaccineSeed pncBabyVaccineSeed;

    @Before
    public void setUp() throws Exception {
        pncBabyVaccineSeed = new PNCBabyVaccineSeed(null, null, null);
    }

    @Test
    public void shouldReturnScheduleNamesBasedOnMilestoneNames(){
        assertThat(pncBabyVaccineSeed.getScheduleName("PNC1"), is(equalTo(ScheduleNames.PNC_CHILD_1)));
        assertThat(pncBabyVaccineSeed.getScheduleName("PNC2"), is(equalTo(ScheduleNames.PNC_CHILD_2)));
        assertThat(pncBabyVaccineSeed.getScheduleName("PNC3"), is(equalTo(ScheduleNames.PNC_CHILD_3)));
        assertThat(pncBabyVaccineSeed.getScheduleName("PNC-CHILD-1"), is(equalTo(ScheduleNames.PNC_CHILD_1)));
        assertThat(pncBabyVaccineSeed.getScheduleName("PNC-CHILD-2"), is(equalTo(ScheduleNames.PNC_CHILD_2)));
        assertThat(pncBabyVaccineSeed.getScheduleName("PNC-CHILD-3"), is(equalTo(ScheduleNames.PNC_CHILD_3)));
    }

    @Test
    public void shouldMapMilestoneNames(){
        assertThat(pncBabyVaccineSeed.mapMilestoneName("PNC1"), is(equalTo("PNC-C1")));
        assertThat(pncBabyVaccineSeed.mapMilestoneName("PNC2"), is(equalTo("PNC-C2")));
        assertThat(pncBabyVaccineSeed.mapMilestoneName("PNC3"), is(equalTo("PNC-C3")));
    }

}
