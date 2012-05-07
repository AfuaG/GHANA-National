package org.motechproject.ghana.national.messagegateway.service;

import org.motechproject.ghana.national.messagegateway.domain.Payload;
import org.motechproject.ghana.national.messagegateway.domain.SMS;
import org.motechproject.sms.api.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// TODO: this class should go to the client of Aggregator module
@Service("payloadDispatcher")
public class PayloadDispatcher {

    @Autowired
    SmsService smsService;

    public void dispatch(Payload payload) {
        if(payload instanceof SMS){
            SMS sms = (SMS) payload;
            smsService.sendSMS(sms.getPhoneNumber(), sms.getText());
        }
    }
}
