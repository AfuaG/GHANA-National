package org.motechproject.ghana.national.ivr;

import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.ivr.AudioPrompts;
import org.motechproject.ghana.national.domain.ivr.MobileMidwifeAudioClips;
import org.motechproject.retry.service.RetryService;
import org.motechproject.testing.utils.BaseUnitTest;
import org.motechproject.util.DateUtil;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class IVRInboundCallIT extends BaseUnitTest {

    private static VerboiceStub verboiceStub;
    private static TestAppServer testAppServer;
    private static Patient patientWithMM;
    private static Patient patientWithMMAndNoMessagesInOutbox;

    public static final String INBOUND_DECISION_TREE_NAME = "InboundDecisionTree";
    private static final String TIMEOUT_IN_SEC = "20";
    private static final String ANYTHING = null;
    private static final String FINISH_ON_KEY = "#";

    @Mock
    private static RetryService retryServiceMock;

    @BeforeClass
    public static void setUp() throws Exception {
        testAppServer = new TestAppServer();
        verboiceStub = new VerboiceStub(testAppServer);
        testAppServer.startApplication();

        patientWithMM = testAppServer.createPatient("patientWithMM", "lastName");
        testAppServer.registerForMobileMidwife(patientWithMM);
        patientWithMMAndNoMessagesInOutbox = testAppServer.createPatient("patientWithMMAndNoMessagesInOutbox", "lastName");
        testAppServer.registerForMobileMidwife(patientWithMMAndNoMessagesInOutbox);

        DateTime now = DateTime.now();
        testAppServer.addCareMessageToOutbox(patientWithMM.getMotechId(), AudioPrompts.TT_DUE.value(), now);
        testAppServer.addCareMessageToOutbox(patientWithMM.getMotechId(), AudioPrompts.IPT_DUE.value(), now.minus(Period.days(1)));
        testAppServer.addCareMessageToOutbox(patientWithMM.getMotechId(), AudioPrompts.PNC_MOTHER_DUE.value(), now.plus(Period.days(1)));
        testAppServer.addAppointmentMessageToOutbox(patientWithMM.getMotechId(), AudioPrompts.ANC_DUE.value());
        testAppServer.addMobileMidwifeMessageToOutbox(patientWithMM.getMotechId(), MobileMidwifeAudioClips.PREGNANCY_WEEK_7);
    }

    @Before
    public void setUpEachTest() {
        initMocks(this);
        testAppServer.mockRetryService(retryServiceMock);
    }

    @AfterClass
    public static void shutdown() {
        testAppServer.shutdownApplication();
    }

    @Test
    public void shouldRedirectToCallcenterOnUserWait() {
        String response = verboiceStub.handleIncomingCall();
        String repeatResponse = verboiceStub.handleTimeout(response);
       validateConnectToCallCenter(repeatResponse);
    }

    private void validateConnectToCallCenter(String response) {
        TwiML callCenterDial = new TwiML().addAction(new TwiML.Dial());
        TwiML callCenterClosed = new TwiML().addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.CALL_CENTER_DIAL_FAILED), "EN")));
        if (!verboiceStub.matches(callCenterDial, response))
            verboiceStub.expect(callCenterClosed, response);
    }

    @Test
    public void shouldConnectToCallCenterIfUserOptedForItWhileAskedToEnterTheMotechIdAfterTheUserHavingEnteredAndInvalidId() {
        String response = verboiceStub.handleIncomingCall();
        String englishLanguageOption = "1";
        response = verboiceStub.handle(response, englishLanguageOption);

        String listenToMMMessages = "1";
        response = verboiceStub.handle(response, listenToMMMessages);

        response = verboiceStub.handle(response, "someinvalidmotechid");

        String connectToCallCenterOption = "0";
        String responseForConnectToCallCenterOption = verboiceStub.handle(response, connectToCallCenterOption);
        validateConnectToCallCenter(responseForConnectToCallCenterOption);

        connectToCallCenterOption = "*";
        responseForConnectToCallCenterOption = verboiceStub.handle(response, connectToCallCenterOption);
        validateConnectToCallCenter(responseForConnectToCallCenterOption);
    }

    @Test
    public void shouldConnectToCallCenterIfUserDoesNotSelectionAnOptionForReasonForTheCall() {
        String response = verboiceStub.handleIncomingCall();
        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);

        String reasonForCallPrompt = fileName(AudioPrompts.REASON_FOR_CALL_PROMPT);
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(reasonForCallPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzE&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        response = verboiceStub.handleTimeout(response);
        validateConnectToCallCenter(response);
    }

    @Test
    public void shouldRetryAskingToEnterMotechIdIfUserHadNotProviedAnInputWithin20Seconds() {
        String response = verboiceStub.handleIncomingCall();

        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);

        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);

        String motechIdPrompt = fileName(AudioPrompts.MOTECH_ID_PROMPT);
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(motechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMQ&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(motechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMS90aW1lb3V0&Digits=timeout")));

        response = verboiceStub.handleTimeout(response);
        verboiceStub.expect(expectedActions, response);

        expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(motechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMS90aW1lb3V0L3RpbWVvdXQ&Digits=timeout")));

        response = verboiceStub.handleTimeout(response);
        verboiceStub.expect(expectedActions, response);

        expectedActions = new TwiML()
                .addAction(new TwiML.Exit());

        response = verboiceStub.handleTimeout(response);
        verboiceStub.expect(expectedActions, response);
    }

    @Test
    public void shouldHandleInboundCallForPatientWithMMRegistrationAndNoMessagesInInbox() {
        String response = verboiceStub.handleIncomingCall();

        String selectLanguagePrompt = fileName(AudioPrompts.LANGUAGE_PROMPT);
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(selectLanguagePrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=Lw&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);
        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);

        String reasonForCallPrompt = fileName(AudioPrompts.REASON_FOR_CALL_PROMPT);
        expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(reasonForCallPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzE&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);

        String motechIdPrompt = fileName(AudioPrompts.MOTECH_ID_PROMPT);
        expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(motechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMQ&Digits=timeout")));
        verboiceStub.expect(expectedActions, response);

        String noMessagesPrompt = fileName(AudioPrompts.NO_MESSAGE_IN_OUTBOX);
        response = verboiceStub.handle(response, patientWithMMAndNoMessagesInOutbox.getMotechId());
        expectedActions = new TwiML().addAction(new TwiML.Play(testAppServer.clipPath(noMessagesPrompt, "EN")));
        verboiceStub.expect(expectedActions, response);
    }

    @Test
    public void shouldConnectToCallCenterOnPressingAsterisk() {
        mockCurrentDate(DateUtil.newDateTime(2012, 9, 5, 10, 0, 0));
        String response = verboiceStub.handleIncomingCall();
        String callcenterButton = "*";
        response = verboiceStub.handle(response, callcenterButton);
        validateConnectToCallCenter(response);
        assertTrue("pressing * key should redirect to a special nurse line.", response.contains("0208516182"));

        response = verboiceStub.handleIncomingCall();
        String languageOption = "1";
        response = verboiceStub.handle(response, languageOption);
        response = verboiceStub.handle(response, callcenterButton);
        validateConnectToCallCenter(response);
        assertTrue("pressing * key should redirect to a special nurse line.", response.contains("0208516182"));

        response = verboiceStub.handleIncomingCall();
        languageOption = "1";
        response = verboiceStub.handle(response, languageOption);
        response = verboiceStub.handle(response, languageOption);
        response = verboiceStub.handle(response, callcenterButton);
        response = verboiceStub.handle(response, callcenterButton);
        validateConnectToCallCenter(response);
        assertTrue("pressing * key should redirect to a special nurse line.", response.contains("0208516182"));
    }

    @Test
    public void shouldConnectToCallCenterOnPressingZero() {
        mockCurrentDate(DateUtil.newDateTime(2012, 9, 5, 10, 0, 0));
        String response = verboiceStub.handleIncomingCall();
        String callcenterButton = "0";
        response = verboiceStub.handle(response, callcenterButton);
        validateConnectToCallCenter(response);
        assertTrue("pressing 0 key should redirect to a language specific call center", response.contains("0206369910"));

        response = verboiceStub.handleIncomingCall();
        String languageOption = "1";
        response = verboiceStub.handle(response, languageOption);
        response = verboiceStub.handle(response, callcenterButton);
        validateConnectToCallCenter(response);
        assertTrue("pressing 0 key should redirect to a language specific call center", response.contains("0206369910"));

        response = verboiceStub.handleIncomingCall();
        languageOption = "1";
        response = verboiceStub.handle(response, languageOption);
        response = verboiceStub.handle(response, languageOption);
        response = verboiceStub.handle(response, callcenterButton);
        response = verboiceStub.handle(response, callcenterButton);
        validateConnectToCallCenter(response);
        assertTrue("pressing 0 key should redirect to a language specific call center", response.contains("0206369910"));
    }

    @Test
    public void shouldHandleInboundCallForInvalidMotechId() {
        String response = verboiceStub.handleIncomingCall();

        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);
        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);
        String invalidMotechId = "1";
        response = verboiceStub.handle(response, invalidMotechId);

        String invalidMotechIdPrompt = fileName(AudioPrompts.INVALID_MOTECH_ID_PROMPT);
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(invalidMotechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMS8x&Digits=timeout")));
        verboiceStub.expect(expectedActions, response);
    }

    @Test
    public void shouldHandleInboundCallIfNotRegisteredToMobileMidwife() {
        Patient patientWithoutMobileMidwifeRegistration = testAppServer.createPatient("patientWithOutMM", "lastName");
        String response = verboiceStub.handleIncomingCall();

        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);
        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);
        response = verboiceStub.handle(response, patientWithoutMobileMidwifeRegistration.getMotechId());

        String invalidMotechIdPrompt = fileName(AudioPrompts.INVALID_MOTECH_ID_PROMPT);
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(invalidMotechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithoutMobileMidwifeRegistration.getMotechId()).getBytes()) + "&Digits=timeout")));
        verboiceStub.expect(expectedActions, response);
    }

    @Test
    public void shouldHangTheCallAfter3InvalidMotechId() {
        String response = verboiceStub.handleIncomingCall();

        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);
        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);

        String motechIdPrompt = fileName(AudioPrompts.MOTECH_ID_PROMPT);
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(motechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMQ&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        String invalidMotechId = "1";
        response = verboiceStub.handle(response, invalidMotechId);

        String invalidMotechIdPrompt = fileName(AudioPrompts.INVALID_MOTECH_ID_PROMPT);
        expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(invalidMotechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + invalidMotechId).getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        response = verboiceStub.handle(response, invalidMotechId);

        expectedActions = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(invalidMotechIdPrompt, "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzEvMS8xLzE&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        response = verboiceStub.handle(response, invalidMotechId);
        expectedActions = new TwiML().addAction(new TwiML.Exit());

        verboiceStub.expect(expectedActions, response);
    }

    @Test
    public void shouldDisconnectTheCallIfUserDoesNotEnterAInputFor20SecForMMMessagePrompt() {

        String response = verboiceStub.handleIncomingCall();
        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);
        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);

        // first message
        response = verboiceStub.handle(response, patientWithMM.getMotechId());
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.IPT_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.TT_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.PNC_MOTHER_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.ANC_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, "0", null))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId()).getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        // cancel retry and proceed
        response = verboiceStub.handleTimeout(response);
        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout").getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        // no action - and timed out
        TwiML expectedConnectToCallCenterActions = new TwiML().addAction(new TwiML.Exit());

        String timeOutResponse = verboiceStub.handleTimeout(response);
        verboiceStub.expect(expectedConnectToCallCenterActions, timeOutResponse);
    }

    @Test
    public void shouldConnectToCallCenterIfUserPressesZero_Or_AsteriskWhileListeningToMMMessages() {

        String response = verboiceStub.handleIncomingCall();
        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);
        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);

        // first message
        response = verboiceStub.handle(response, patientWithMM.getMotechId());
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.IPT_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.TT_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.PNC_MOTHER_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.ANC_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, "0", null))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId()).getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        // cancel retry and proceed
        response = verboiceStub.handleTimeout(response);
        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout").getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        // invalid input
        String invalidInputResponse = verboiceStub.handle(response, "0");
        validateConnectToCallCenter(invalidInputResponse);


        String hangupResponse = verboiceStub.handle(response, "invalid");
        TwiML expectedXML = new TwiML().addAction(new TwiML.Exit());
        verboiceStub.expect(expectedXML, hangupResponse);

        // play second message from first message menu
        String playSecondMessageOption = "2";
        String secondMessageResponse = verboiceStub.handle(response, playSecondMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(1)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(1)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/2").getBytes()) + "&Digits=timeout")));


        verboiceStub.expect(expectedActions, secondMessageResponse);

        // invalid input
        invalidInputResponse = verboiceStub.handle(secondMessageResponse, "*");
        validateConnectToCallCenter(invalidInputResponse);

        hangupResponse = verboiceStub.handle(secondMessageResponse, "invalid");
        expectedXML = new TwiML().addAction(new TwiML.Exit());
        verboiceStub.expect(expectedXML, hangupResponse);
    }

    @Test
    public void shouldHandleInboundCallForPatientWithMMRegistrationAndWithMessagesInInbox() {

        String response = verboiceStub.handleIncomingCall();
        String englishLanguageChoice = "1";
        response = verboiceStub.handle(response, englishLanguageChoice);
        String listenMessagesChoice = "1";
        response = verboiceStub.handle(response, listenMessagesChoice);

        // first message
        response = verboiceStub.handle(response, patientWithMM.getMotechId());
        TwiML expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.IPT_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.TT_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.PNC_MOTHER_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.ANC_DUE), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, "0", null))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId()).getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        // cancel retry and proceed
        response = verboiceStub.handleTimeout(response);
        verify(retryServiceMock).fulfill(patientWithMM.getMotechId(), Constants.RETRY_GROUP);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout").getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);

        // repeat first message
        String repeatOption = "1";
        String repeatResponse = verboiceStub.handle(response, repeatOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(0)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(0)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1").getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, repeatResponse);

        // play second message from first message menu
        String playSecondMessageOption = "2";
        String secondMessageResponse = verboiceStub.handle(repeatResponse, playSecondMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(1)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(1)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/2").getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, secondMessageResponse);

        // repeat second message
        String repeatSecondMessageOption = "1";
        String secondMessageRepeatResponse = verboiceStub.handle(secondMessageResponse, repeatSecondMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(1)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(1)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/2/1").getBytes()) + "&Digits=timeout")));


        verboiceStub.expect(expectedActions, secondMessageRepeatResponse);

        // play third message from second message menu

        String playThirdMessageOption = "2";
        String thirdMessageResponse = verboiceStub.handle(secondMessageRepeatResponse, playThirdMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(2)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(2)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/2/1/2").getBytes()) + "&Digits=timeout")));


        verboiceStub.expect(expectedActions, thirdMessageResponse);

        // repeat third message

        String repeatThirdMessageOption = "1";
        String thirdMessageRepeatResponse = verboiceStub.handle(thirdMessageResponse, repeatThirdMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(2)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(2)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/2/1/2/1").getBytes()) + "&Digits=timeout")));


        verboiceStub.expect(expectedActions, thirdMessageRepeatResponse);

        // play second message from third message menu

        String secondMessageOption = "2";
        secondMessageResponse = verboiceStub.handle(thirdMessageRepeatResponse, secondMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(1)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(1)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/2/1/2/1/2").getBytes()) + "&Digits=timeout")));


        verboiceStub.expect(expectedActions, secondMessageResponse);

        // play third message from first message menu
        playThirdMessageOption = "3";
        response = verboiceStub.handle(repeatResponse, playThirdMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(2)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(2)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/3").getBytes()) + "&Digits=timeout")));


        verboiceStub.expect(expectedActions, response);

        // play third message from first message menu
        playSecondMessageOption = "2";
        response = verboiceStub.handle(response, playSecondMessageOption);

        expectedActions = new TwiML()
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getClipNames().get(1)), "EN")))
                .addAction(new TwiML.Play(testAppServer.clipPath(fileName(MobileMidwifeAudioClips.PREGNANCY_WEEK_7.getPromptClipNames().get(1)), "EN")))
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, FINISH_ON_KEY))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=" + Base64.encodeBase64URLSafeString(("/1/1/" + patientWithMM.getMotechId() + "/timeout/1/3/2").getBytes()) + "&Digits=timeout")));

        verboiceStub.expect(expectedActions, response);
    }

    @Test
    public void shouldRetryThreeTimesOnInvalidKeyAtTheStart() {
        String response = verboiceStub.handleIncomingCall();
        TwiML expectedAction1 = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.LANGUAGE_PROMPT), "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzY&Digits=timeout")));
        TwiML expectedAction2 = new TwiML()
                .addAction(new TwiML.Gather(ANYTHING, TIMEOUT_IN_SEC, null).addPrompt(new TwiML.Play(testAppServer.clipPath(fileName(AudioPrompts.LANGUAGE_PROMPT), "EN"))))
                .addAction(new TwiML.Redirect(testAppServer.treePath(INBOUND_DECISION_TREE_NAME, "&trP=LzYvNw&Digits=timeout")));
        TwiML expectedAction3 = new TwiML().addAction(new TwiML.Exit());
        String repeatResponse = verboiceStub.handle(response, "6");
        verboiceStub.expect(expectedAction1, repeatResponse);
        String repeatResponse2 = verboiceStub.handle(repeatResponse, "7");
        verboiceStub.expect(expectedAction2, repeatResponse2);
        String repeatResponse3 = verboiceStub.handle(repeatResponse2, "8");
        verboiceStub.expect(expectedAction3, repeatResponse3);
    }

    private String fileName(AudioPrompts audioPrompt) {
        return audioPrompt.value() + ".wav";
    }

    private String fileName(String clipName) {
        return clipName + ".wav";
    }

}
