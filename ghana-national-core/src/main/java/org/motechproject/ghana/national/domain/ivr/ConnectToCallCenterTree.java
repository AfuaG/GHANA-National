package org.motechproject.ghana.national.domain.ivr;

import org.codehaus.jackson.annotate.JsonProperty;
import org.motechproject.decisiontree.FlowSession;
import org.motechproject.decisiontree.model.AudioPrompt;
import org.motechproject.decisiontree.model.DialPrompt;
import org.motechproject.decisiontree.model.Node;
import org.motechproject.decisiontree.model.Transition;
import org.motechproject.ghana.national.builder.IVRCallbackUrlBuilder;
import org.motechproject.ghana.national.domain.IVRClipManager;
import org.motechproject.ghana.national.domain.mobilemidwife.Language;
import org.motechproject.ghana.national.service.IvrCallCenterNoMappingService;
import org.motechproject.model.DayOfWeek;
import org.motechproject.model.Time;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectToCallCenterTree extends Transition{

    @Autowired
    private IVRCallbackUrlBuilder ivrCallbackUrlBuilder;

    @Autowired
    private IVRClipManager ivrClipManager;

    @Autowired
    private IvrCallCenterNoMappingService ivrCallCenterNoMappingService;

    @JsonProperty
    private Language language;

    // Required for Ektorp
    public ConnectToCallCenterTree() {
    }

    public ConnectToCallCenterTree(Language language) {
        this.language = language;
    }

    @Override
    public Node getDestinationNode(String input, FlowSession session) {
        return getAsNode(language);
    }

    public Node getAsNode(Language language) {
        DayOfWeek dayOfWeek = DayOfWeek.getDayOfWeek(DateUtil.today().dayOfWeek().get());
        String callCenterPhoneNumber = ivrCallCenterNoMappingService.getCallCenterPhoneNumber(language, dayOfWeek, new Time(DateUtil.now().toLocalTime()));

        if(callCenterPhoneNumber == null) {
            return new Node().addPrompts(new AudioPrompt().setAudioFileUrl(ivrClipManager.urlFor(AudioPrompts.CALL_CENTER_DIAL_FAILED.value(), Language.EN)));
        }

        DialPrompt callCenterDialPrompt = new DialPrompt(callCenterPhoneNumber);
        callCenterDialPrompt.setCallerId(callCenterPhoneNumber);
        callCenterDialPrompt.setAction(ivrCallbackUrlBuilder.callCenterDialStatusUrl(language));
        return new Node().addPrompts(callCenterDialPrompt);
    }
}
