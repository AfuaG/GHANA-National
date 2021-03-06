package org.motechproject.ghana.national.validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.ClientQueryForm;
import org.motechproject.ghana.national.domain.ClientQueryType;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.mobileforms.api.domain.FormBean;
import org.motechproject.mobileforms.api.domain.FormBeanGroup;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.domain.Constants.*;

public class ClientQueryFormValidatorTest {
    private ClientQueryFormValidator clientQueryFormValidator;
    @Mock
    private FormValidator formValidator;

    @Before
    public void setUp() {
        initMocks(this);
        clientQueryFormValidator = new ClientQueryFormValidator();
        ReflectionTestUtils.setField(clientQueryFormValidator, "formValidator", formValidator);
    }

    @Test
    public void shouldValidateTheClientQueryFormBean() {
        String facilityId = "13161";
        String motechId = "123458";
        String staffId = "23";
        ClientQueryForm clientQueryForm = createClientQueryForm(facilityId, motechId, staffId, ClientQueryType.CLIENT_DETAILS.toString());

        when(formValidator.validateIfFacilityExists(facilityId)).thenReturn(asList(new FormError(facilityId, NOT_FOUND)));
        when(formValidator.validateIfStaffExists(staffId)).thenReturn(asList(new FormError(staffId, NOT_FOUND)));
        when(formValidator.getPatient(motechId)).thenReturn(new Patient(new MRSPatient(motechId,new MRSPerson().dead(true),new MRSFacility(facilityId))));
        List<FormBean> formBeans = Arrays.<FormBean>asList(clientQueryForm);
        List<FormError> formErrors = clientQueryFormValidator.validate(clientQueryForm, new FormBeanGroup(formBeans), formBeans);

        assertThat(formErrors, hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME, Constants.IS_NOT_ALIVE)));
        assertThat(formErrors, hasItem(new FormError(facilityId, NOT_FOUND)));
        assertThat(formErrors, hasItem(new FormError(staffId, NOT_FOUND)));
    }

    @Test
    public void shouldNotValidateForPatientIfQueryTypeIsFindClientId(){
       String facilityId = "13161";
        String staffId = "23";
        ClientQueryForm clientQueryForm = createClientQueryForm(facilityId, "13245", staffId, ClientQueryType.FIND_CLIENT_ID.toString());

        when(formValidator.validateIfFacilityExists(facilityId)).thenReturn(asList(new FormError(facilityId, NOT_FOUND)));
        when(formValidator.validateIfStaffExists(staffId)).thenReturn(asList(new FormError(staffId, NOT_FOUND)));

        List<FormBean> formBeans = Arrays.<FormBean>asList(clientQueryForm);
        List<FormError> formErrors = clientQueryFormValidator.validate(clientQueryForm, new FormBeanGroup(formBeans), formBeans);

        assertThat(formErrors, hasItem(new FormError(facilityId, NOT_FOUND)));
        assertThat(formErrors, hasItem(new FormError(staffId, NOT_FOUND)));
        assertThat(formErrors, hasItem(new FormError("queryType", INSUFFICIENT_SEARCH_CRITERIA)));
    }

    @Test
    public void shouldValidateTheClientQueryFormBeanForUpComingCare() {
        String facilityId = "13161";
        String motechId = "123458";
        String staffId = "23";
        ClientQueryForm clientQueryForm = createClientQueryForm(facilityId, motechId, staffId, ClientQueryType.UPCOMING_CARE.toString());

        when(formValidator.validateIfFacilityExists(facilityId)).thenReturn(asList(new FormError(facilityId, NOT_FOUND)));
        when(formValidator.validateIfStaffExists(staffId)).thenReturn(asList(new FormError(staffId, NOT_FOUND)));
        when(formValidator.getPatient(motechId)).thenReturn(new Patient(new MRSPatient(motechId,new MRSPerson().dead(true),new MRSFacility(facilityId))));

        List<FormBean> formBeans = Arrays.<FormBean>asList(clientQueryForm);
        List<FormError> formErrors = clientQueryFormValidator.validate(clientQueryForm, new FormBeanGroup(formBeans), formBeans);

        assertThat(formErrors, hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME, Constants.IS_NOT_ALIVE)));
        assertThat(formErrors, hasItem(new FormError(facilityId, NOT_FOUND)));
        assertThat(formErrors, hasItem(new FormError(staffId, NOT_FOUND)));
    }

    private ClientQueryForm createClientQueryForm(String facilityId, String motechId, String staffId, String clientDetails) {
        ClientQueryForm clientQueryForm = new ClientQueryForm();
        clientQueryForm.setFacilityId(facilityId);
        clientQueryForm.setMotechId(motechId);
        clientQueryForm.setStaffId(staffId);
        clientQueryForm.setQueryType(clientDetails);
        return clientQueryForm;
    }
}
