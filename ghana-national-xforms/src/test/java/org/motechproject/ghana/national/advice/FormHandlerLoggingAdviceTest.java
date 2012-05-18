package org.motechproject.ghana.national.advice;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.ClientDeathForm;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.handler.CareScheduleHandler;
import org.motechproject.ghana.national.handlers.ClientDeathFormHandler;
import org.motechproject.ghana.national.logger.advice.FormHandlerLoggingAdvice;
import org.motechproject.ghana.national.repository.AllObservations;
import org.motechproject.ghana.national.repository.SMSGateway;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.MobileMidwifeService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;
import org.motechproject.scheduletracking.api.domain.Milestone;
import org.motechproject.scheduletracking.api.domain.MilestoneAlert;
import org.motechproject.scheduletracking.api.domain.WindowName;
import org.motechproject.scheduletracking.api.events.MilestoneEvent;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-xforms-test.xml"})
public class FormHandlerLoggingAdviceTest {

    @Mock
    private PatientService patientService;

    @Mock
    private FacilityService facilityService;

    @Mock
    private MobileMidwifeService mobileMidwifeService;

    @Mock
    private AllObservations allObservations;

    @Mock
    private SMSGateway smsGateway;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private FormHandlerLoggingAdvice formHandlerLoggingAdvice;

    @Autowired
    @Qualifier("clientDeathFormHandler")
    private FormPublishHandler clientDeathFormHandler;

    @Autowired
    private CareScheduleHandler careScheduleHandler;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(patientService.patientByOpenmrsId(Matchers.<String>any())).thenReturn(new Patient(new MRSPatient("motechId", new MRSPerson().firstName("firstName").lastName("lastName").dateOfBirth(DateTime.now().toDate()), new MRSFacility("id"))));
        when(facilityService.getFacility(Matchers.<String>any())).thenReturn(new Facility().motechId("facilityMotechId"));

        ReflectionTestUtils.setField(formHandlerLoggingAdvice, "jdbcTemplate", jdbcTemplate);
        mockFormPublishHandler();
        mockCareScheduleHandler();
    }

    @Test
    public void shouldLog(){
        final ClientDeathForm clientDeathForm = new ClientDeathForm();
        clientDeathFormHandler.handleFormEvent(new MotechEvent("subject", new HashMap<String, Object>() {{
            put(Constants.FORM_BEAN, clientDeathForm);
        }}));
        MilestoneAlert milestoneAlert = MilestoneAlert.fromMilestone(new Milestone("milestone", Period.ZERO, Period.days(1), Period.hours(12), Period.months(1)), DateTime.now());
        careScheduleHandler.handlePregnancyAlert(new MilestoneEvent(null, null, milestoneAlert, WindowName.earliest.name(), null));
        verify(jdbcTemplate, times(2)).execute(anyString());
    }

    private void mockCareScheduleHandler() throws Exception{
        final CareScheduleHandler proxiedCareScheduleHandler = (CareScheduleHandler)((Advised) careScheduleHandler).getTargetSource().getTarget();
        ReflectionTestUtils.setField(proxiedCareScheduleHandler, "patientService", patientService);
        ReflectionTestUtils.setField(proxiedCareScheduleHandler, "facilityService", facilityService);
        ReflectionTestUtils.setField(proxiedCareScheduleHandler, "allObservations", allObservations);
        ReflectionTestUtils.setField(proxiedCareScheduleHandler, "smsGateway", smsGateway);
    }

    private void mockFormPublishHandler() throws Exception {
        final ClientDeathFormHandler proxiedClientDeathFormHandler = (ClientDeathFormHandler)((Advised) clientDeathFormHandler).getTargetSource().getTarget();
        ReflectionTestUtils.setField(proxiedClientDeathFormHandler, "mobileMidwifeService", mobileMidwifeService);
        ReflectionTestUtils.setField(proxiedClientDeathFormHandler, "patientService", patientService);

    }
}
