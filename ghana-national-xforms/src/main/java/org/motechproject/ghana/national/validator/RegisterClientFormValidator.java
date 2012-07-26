package org.motechproject.ghana.national.validator;

import org.apache.commons.lang.StringUtils;
import org.motechproject.ghana.national.bean.RegisterClientForm;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.domain.RegistrationType;
import org.motechproject.ghana.national.helper.FormWithHistoryInput;
import org.motechproject.ghana.national.validator.patient.*;
import org.motechproject.mobileforms.api.domain.FormBean;
import org.motechproject.mobileforms.api.domain.FormBeanGroup;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mobileforms.api.validator.FormValidator;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.motechproject.ghana.national.domain.Constants.NOT_FOUND;

@Component
public class RegisterClientFormValidator extends FormValidator<RegisterClientForm> {

    @Autowired
    private org.motechproject.ghana.national.validator.FormValidator formValidator;

    @Override
    @LoginAsAdmin
    @ApiSession
    public List<FormError> validate(RegisterClientForm formBean, FormBeanGroup group, List<FormBean> allForms) {
        List<FormError> formErrors = super.validate(formBean, group, allForms);

        Patient patient = null;
        if(formBean.getMotechId() != null)
            patient = formValidator.getPatient(formBean.getMotechId());


        formErrors.addAll(formValidator.validateIfStaffExists(formBean.getStaffId()));
        formErrors.addAll(formValidator.validateIfFacilityExists(formBean.getFacilityId()));

        PatientValidator validators = patientValidator(formBean, formBean.getRegistrationMode(), formBean.getDateOfBirth(), formBean.getSex(), formBean.getRegistrantType(), formBean.getMotherMotechId());
        List<FormError> patientValidationErrors = getDependentValidator().validate(patient, group.getFormBeans(), allForms, validators);
        formErrors.addAll(patientValidationErrors);

        formErrors.addAll(formValidator.validateNHISExpiry(formBean.getNhisExpires()));

        return formErrors;
    }

    public PatientValidator patientValidator(FormWithHistoryInput formWithHistoryInput, RegistrationType registrationModeFromForm, Date dateOfBirthFromForm, String sexFromForm, PatientType registrantTypeFromForm, String motherMotechIdFromForm) {
        Patient patientsMother = null;
        if(motherMotechIdFromForm != null)
            patientsMother = formValidator.getPatient(motherMotechIdFromForm);

        String mothersMotechId = "Mothers motech Id";

        PatientValidator validators = new AlwaysValid();
        //do not inline
        validators
            .onSuccess(true, new NotExistsInDb(), RegistrationType.USE_PREPRINTED_ID.equals(registrationModeFromForm))
            .onSuccess(true, new IsFormSubmittedForAChild(dateOfBirthFromForm, null), PatientType.CHILD_UNDER_FIVE.equals(registrantTypeFromForm))
            .onSuccess(true, new IsFormSubmittedForAFemale(sexFromForm), PatientType.PREGNANT_MOTHER.equals(registrantTypeFromForm))
            .onSuccess(new IsFormSubmittedWithValidHistoryDates(dateOfBirthFromForm, formWithHistoryInput))
            .onSuccess(true, new ExistsInDb(patientsMother, new FormError(mothersMotechId, NOT_FOUND))
                    .onFailure(new RegClientFormSubmittedInSameUploadForMotechId(motherMotechIdFromForm, new FormError(mothersMotechId, NOT_FOUND))), PatientType.CHILD_UNDER_FIVE.equals(registrantTypeFromForm) && !StringUtils.isEmpty(motherMotechIdFromForm));

        return validators;
    }


    DependentValidator getDependentValidator() {
        return new DependentValidator();
    }

}

