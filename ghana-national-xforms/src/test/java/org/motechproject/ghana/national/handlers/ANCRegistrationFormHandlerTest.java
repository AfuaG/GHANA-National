package org.motechproject.ghana.national.handlers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.RegisterANCForm;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.domain.RegistrationToday;
import org.motechproject.ghana.national.service.ANCService;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.vo.ANCVO;
import org.motechproject.model.MotechEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;


public class ANCRegistrationFormHandlerTest {

    private ANCRegistrationFormHandler ancRegistrationFormHandler;
    @Mock
    private ANCService mockANCService;
    @Mock
    private FacilityService mockFacilityService;


    @Before
    public void setUp() {
        initMocks(this);
        ancRegistrationFormHandler = new ANCRegistrationFormHandler();
        ReflectionTestUtils.setField(ancRegistrationFormHandler, "ancService", mockANCService);
        ReflectionTestUtils.setField(ancRegistrationFormHandler, "facilityService", mockFacilityService);
    }

    @Test
    public void shouldSaveANCEnrollmentDetails() {
        final RegisterANCForm registerANCForm = new RegisterANCForm();
        registerANCForm.setAddHistory(true);
        registerANCForm.setAncRegNumber("12432423423");
        registerANCForm.setDate(new Date());
        registerANCForm.setDeliveryDateConfirmed(true);
        registerANCForm.setEstDeliveryDate(new Date(2012, 3, 4));
        registerANCForm.setFacilityId("12345");
        registerANCForm.setGravida(3);
        registerANCForm.setHeight(4.67);
        registerANCForm.setLastIPT("4");
        registerANCForm.setLastIPTDate(new Date(2011, 8, 8));
        registerANCForm.setLastTT("5");
        registerANCForm.setLastTTDate(new Date(2011, 7, 6));
        registerANCForm.setMotechId("343423423");
        registerANCForm.setParity(3);
        registerANCForm.setRegDateToday(RegistrationToday.IN_PAST);
        registerANCForm.setRegPhone("045353453434");
        registerANCForm.setStaffId("2331");

        Facility facility = mock(Facility.class);
        String mrsFacilityId = "343";

        when(mockFacilityService.getFacilityByMotechId(registerANCForm.getFacilityId())).thenReturn(facility);
        when(facility.getMrsFacilityId()).thenReturn(mrsFacilityId);
        ancRegistrationFormHandler.handleFormEvent(new MotechEvent("form.validation.successful.NurseDataEntry.registerANC", new HashMap<String, Object>() {{
            put("formBean", registerANCForm);
        }}));

        final ArgumentCaptor<ANCVO> captor = ArgumentCaptor.forClass(ANCVO.class);
        verify(mockANCService).enroll(captor.capture(), eq(Constants.ENCOUNTER_ANCREGVISIT));
        final ANCVO ancVO = captor.getValue();

        assertEquals(registerANCForm.getAddHistory(), ancVO.getAddHistory());
        assertEquals(registerANCForm.getAncRegNumber(), ancVO.getSerialNumber());
        assertEquals(registerANCForm.getDate(), ancVO.getRegistrationDate());
        assertEquals(registerANCForm.getDeliveryDateConfirmed(), ancVO.getDeliveryDateConfirmed());
        assertEquals(registerANCForm.getEstDeliveryDate(), ancVO.getEstimatedDateOfDelivery());
        assertEquals(registerANCForm.getGravida(), ancVO.getGravida());
        assertEquals(registerANCForm.getHeight(), ancVO.getHeight());
        assertEquals(registerANCForm.getLastIPT(), ancVO.getLastIPT());
        assertEquals(registerANCForm.getLastIPTDate(), ancVO.getLastIPTDate());
        assertEquals(registerANCForm.getLastTT(), ancVO.getLastTT());
        assertEquals(registerANCForm.getLastTTDate(), ancVO.getLastTTDate());
        assertEquals(registerANCForm.getMotechId(), ancVO.getMotechPatientId());
        assertEquals(registerANCForm.getParity(), ancVO.getParity());
        assertEquals(registerANCForm.getRegDateToday(), ancVO.getRegistrationToday());
        assertEquals(registerANCForm.getStaffId(), ancVO.getStaffId());
        assertEquals(mrsFacilityId, ancVO.getFacilityId());
    }

}
