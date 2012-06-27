package org.motechproject.ghana.national.handler;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.motechproject.cmslite.api.model.ContentNotFoundException;
import org.motechproject.ghana.national.builder.IVRCallbackUrlBuilder;
import org.motechproject.ghana.national.builder.IVRRequestBuilder;
import org.motechproject.ghana.national.builder.RetryRequestBuilder;
import org.motechproject.ghana.national.domain.ivr.MobileMidwifeAudioClips;
import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.exception.EventHandlerException;
import org.motechproject.ghana.national.repository.AllPatientsOutbox;
import org.motechproject.ghana.national.repository.IVRGateway;
import org.motechproject.ghana.national.repository.SMSGateway;
import org.motechproject.ghana.national.service.MobileMidwifeService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.retry.service.RetryService;
import org.motechproject.scheduler.domain.MotechEvent;
import org.motechproject.server.event.annotations.MotechListener;
import org.motechproject.server.messagecampaign.EventKeys;
import org.motechproject.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.motechproject.server.messagecampaign.EventKeys.MESSAGE_CAMPAIGN_SEND_EVENT_SUBJECT;

@Component
public class MobileMidwifeCampaignEventHandler {

    private Logger logger = LoggerFactory.getLogger(MobileMidwifeCampaignEventHandler.class);

    @Autowired
    private MobileMidwifeService mobileMidwifeService;
    @Autowired
    private AllPatientsOutbox allPatientsOutbox;
    @Autowired
    private SMSGateway smsGateway;
    @Autowired
    private PatientService patientService;
    @Autowired
    private IVRGateway ivrGateway;
    @Autowired
    private IVRCallbackUrlBuilder ivrCallbackUrlBuilder;
    @Autowired
    private RetryService retryService;


    @MotechListener(subjects = {MESSAGE_CAMPAIGN_SEND_EVENT_SUBJECT})
    public void sendProgramMessage(MotechEvent event) {
        try {
            Map params = event.getParameters();
            String patientId = (String) params.get(EventKeys.EXTERNAL_ID_KEY);

            MobileMidwifeEnrollment enrollment = mobileMidwifeService.findActiveBy(patientId);
            String generatedMessageKey = (String) event.getParameters().get(EventKeys.GENERATED_MESSAGE_KEY);

            if (event.isLastEvent()) mobileMidwifeService.rollover(patientId, DateTime.now());
            sendMessage(enrollment, generatedMessageKey);
        } catch (Exception e) {
            logger.error("<MobileMidwifeEvent>: Encountered error while sending alert: ", e);
            throw new EventHandlerException(event, e);
        }
    }

    private void sendMessage(MobileMidwifeEnrollment enrollment, String messageKey) throws ContentNotFoundException {
        if (Medium.SMS.equals(enrollment.getMedium())) {
            smsGateway.dispatchSMS(messageKey, enrollment.getLanguage().name(), enrollment.getPhoneNumber());
        } else if (Medium.VOICE.equals(enrollment.getMedium())) {
            placeMobileMidwifeMessagesToOutbox(enrollment, messageKey);
            retryService.schedule(RetryRequestBuilder.ivrRetryReqest(enrollment.getPatientId(), DateUtil.now()));
            ivrGateway.placeCall(enrollment.getPhoneNumber(), IVRRequestBuilder.build(ivrCallbackUrlBuilder.outboundCallUrl(enrollment.getPatientId(), enrollment.getLanguage().name(), "OutboundDecisionTree")));
        }
    }

    private void placeMobileMidwifeMessagesToOutbox(MobileMidwifeEnrollment enrollment, String messageKey) {
        allPatientsOutbox.addMobileMidwifeMessage(enrollment.getPatientId(), MobileMidwifeAudioClips.instance(enrollment.getServiceType().getValue(), messageKey), Period.weeks(1));
    }
}
