package org.motechproject.ghana.national.tools.seed.data;

import org.motechproject.ghana.national.domain.IVRChannelMapping;
import org.motechproject.ghana.national.repository.AllIvrChannelMappings;
import org.motechproject.ghana.national.tools.seed.Seed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("ivrChannelMappingSeed")
public class IVRChannelMappingSeed extends Seed {
    Logger logger = LoggerFactory.getLogger(IVRChannelMappingSeed.class);

    @Autowired
    AllIvrChannelMappings allIvrChannelMappings;

    @Override
    protected void load() {
        try {
            allIvrChannelMappings.removeAll();
            Map<String, String> ivrChannelMappings = new HashMap<String, String>(){{
                put("^0(2|5)4[0-9]+$", "MTN");
                put("^0(2|3)0[0-9]+$", "Vodafone");
                put("^0(2|5)7[0-9]+$", "Tigo");
                put("^026[0-9]+$", "Airtel");
                put("^028[0-9]+$", "Expresso");
            }};

            for (Map.Entry<String, String> channelNameAndPhoneNoPattern : ivrChannelMappings.entrySet()) {
                allIvrChannelMappings.add(new IVRChannelMapping().ivrChannel(channelNameAndPhoneNoPattern.getValue()).phoneNumberPattern(channelNameAndPhoneNoPattern.getKey()));
            }

        } catch (Exception e) {
            logger.error("Exception occurred while uploading verboice channel to phone number pattern mapping", e);
        }
    }
}
