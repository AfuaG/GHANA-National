package org.motechproject.ghana.national.validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.CWCVisitForm;
import org.motechproject.ghana.national.bean.RegisterCWCForm;
import org.motechproject.ghana.national.bean.RegisterClientForm;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.mobileforms.api.domain.FormBean;
import org.motechproject.mobileforms.api.domain.FormBeanGroup;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mrs.model.MRSEncounter;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.domain.Constants.CHILD_AGE_MORE_ERR_MSG;
import static org.motechproject.ghana.national.domain.Constants.CHILD_AGE_PARAMETER;
import static org.motechproject.ghana.national.domain.Constants.IS_NOT_ALIVE;
import static org.motechproject.ghana.national.domain.Constants.MOTECH_ID_ATTRIBUTE_NAME;
import static org.motechproject.ghana.national.domain.Constants.NOT_FOUND;
import static org.motechproject.ghana.national.domain.EncounterType.CWC_REG_VISIT;

public class CwcVisitFormValidatorTest {

    @Mock
    private FormValidator formValidator;

    @Mock
    private AllEncounters mockAllEncounters;

    @InjectMocks
    private CwcVisitFormValidator validator= new CwcVisitFormValidator();

    @Before
    public void setUp() {
        initMocks(this);
    }

    @Test
    public void shouldValidateCWCVisitForm() {
        CWCVisitForm formBean = new CWCVisitForm();
        String motechId = "1231231";
        formBean.setMotechId(motechId);
        formBean.setStaffId("123");
        formBean.setVisitor(false);
        formBean.setFacilityId("1234");

        Patient patient = new Patient(new MRSPatient(motechId, new MRSPerson().dead(false), new MRSFacility("facilityId")));
        patient.getMrsPatient().getPerson().age(3);
        List<FormBean> formBeans = Arrays.<FormBean>asList(formBean);
        FormBeanGroup formBeanGroup = new FormBeanGroup(formBeans);

        //patient exists in db,registered for CWC
        MRSEncounter encounter = mock(MRSEncounter.class);
        when(formValidator.getPatient(motechId)).thenReturn(patient);
        when(mockAllEncounters.getLatest(motechId, CWC_REG_VISIT.value())).thenReturn(encounter);
        List<FormError> errors = validator.validate(formBean, formBeanGroup, formBeans);
        assertRegCWCDependencyHasNoError(errors);

        //patient exists in db,incorrect age
        patient.getMrsPatient().getPerson().age(6);
        when(formValidator.getPatient(motechId)).thenReturn(patient);
        errors = validator.validate(formBean, formBeanGroup, formBeans);
        assertThat(errors, hasItem(new FormError(CHILD_AGE_PARAMETER, CHILD_AGE_MORE_ERR_MSG)));

        //patient exists in db,no cwc form submitted
        patient.getMrsPatient().getPerson().age(4);
        when(formValidator.getPatient(motechId)).thenReturn(patient);
        when(mockAllEncounters.getLatest(motechId, CWC_REG_VISIT.value())).thenReturn(null);
        errors = validator.validate(formBean, formBeanGroup, formBeans);
        assertThat(errors, hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME, "not registered for CWC")));

        //patient not in db,no form submitted
        when(formValidator.getPatient(motechId)).thenReturn(null);
        when(mockAllEncounters.getLatest(motechId, CWC_REG_VISIT.value())).thenReturn(null);
        errors = validator.validate(formBean, formBeanGroup, formBeans);
        assertThat(errors, hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME, NOT_FOUND)));

        //patient exists in db,reg cwc form submitted
        when(formValidator.getPatient(motechId)).thenReturn(patient);
        RegisterCWCForm registerCWCForm = new RegisterCWCForm();
        registerCWCForm.setFormname("registerCWC");
        registerCWCForm.setMotechId(motechId);
        formBeanGroup = new FormBeanGroup(Arrays.<FormBean>asList(formBean, registerCWCForm));
        errors = validator.validate(formBean, formBeanGroup, Arrays.<FormBean>asList(formBean, registerCWCForm));
        assertRegCWCDependencyHasNoError(errors);

        //patient not in db,reg client form submitted with CWC
        when(formValidator.getPatient(motechId)).thenReturn(null);
        RegisterClientForm registerClientForm = new RegisterClientForm();
        registerClientForm.setFormname("registerPatient");
        registerClientForm.setMotechId(motechId);
        registerClientForm.setRegistrantType(PatientType.CHILD_UNDER_FIVE);
        formBeanGroup=new FormBeanGroup(Arrays.<FormBean>asList(registerClientForm, formBean));
        errors = validator.validate(formBean,formBeanGroup, Arrays.<FormBean>asList(formBean, registerCWCForm));
        assertRegCWCDependencyHasNoError(errors);
    }

    @Test
    public void shouldNotValidateForPatientIfVisitor() {
        CWCVisitForm formBean = new CWCVisitForm();
        String motechId = "motechId";
        formBean.setMotechId(motechId);
        formBean.setVisitor(true);
        formBean.setStaffId("staffId");
        formBean.setFacilityId("facilityId");

        FormBeanGroup formBeanGroup = new FormBeanGroup(Collections.<FormBean>emptyList());
        validator.validate(formBean, formBeanGroup, Arrays.<FormBean>asList(formBean));
        verify(formValidator).validateIfStaffExists(formBean.getStaffId());
        verify(formValidator).validateIfFacilityExists(formBean.getFacilityId());
        verifyNoMoreInteractions(formValidator);
    }

    private void assertRegCWCDependencyHasNoError(List<FormError> errors) {
        assertThat(errors, not(hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME,NOT_FOUND))));
        assertThat(errors,not(hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME,IS_NOT_ALIVE))));
        assertThat(errors,not(hasItem(new FormError(CHILD_AGE_PARAMETER, CHILD_AGE_MORE_ERR_MSG))));
        assertThat(errors, not(hasItem(new FormError(MOTECH_ID_ATTRIBUTE_NAME, "not registered for CWC"))));
        assertThat(errors,not(hasItem(new FormError("Client type", "should be a child"))));
    }
}
