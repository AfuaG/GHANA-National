package org.motechproject.ghana.national.validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.MobileMidwifeForm;
import org.motechproject.mobileforms.api.domain.FormError;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MobileMidwifeFormValidatorTest {
    private MobileMidwifeFormValidator mobileMidwifeFormValidator;

    @Mock
    private org.motechproject.ghana.national.validator.FormValidator formValidator;

    @Before
    public void setUp(){
        initMocks(this);
        mobileMidwifeFormValidator = new MobileMidwifeFormValidator();
        ReflectionTestUtils.setField(mobileMidwifeFormValidator, "formValidator", formValidator);
    }

    @Test
    public void shouldValidateMobileMidwifeForm() {
        MobileMidwifeForm formBean = mock(MobileMidwifeForm.class);
        String patientId = "1231231";
        String staffId = "11";
        String facilityId = "34";
        when(formBean.getPatientId()).thenReturn(patientId);
        when(formBean.getStaffId()).thenReturn(staffId);
        when(formBean.getFacilityId()).thenReturn(facilityId);

        mobileMidwifeFormValidator.validate(formBean);

        verify(formValidator).validatePatient(Matchers.<List<FormError>>any(), eq(patientId));
        verify(formValidator).validateIfStaffExists(Matchers.<List<FormError>>any(), eq(staffId));
        verify(formValidator).validateIfFacilityExists(Matchers.<List<FormError>>any(), eq(facilityId));
    }
}