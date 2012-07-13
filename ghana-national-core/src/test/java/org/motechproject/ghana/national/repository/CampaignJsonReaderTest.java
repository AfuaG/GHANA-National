package org.motechproject.ghana.national.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
public class CampaignJsonReaderTest {

    @Autowired
    CampaignJsonReader campaignJsonReader;


    @Test
    public void shouldListMobilemidwifeCamaignRecords() {
        System.out.print(campaignJsonReader.records);
    }
}
