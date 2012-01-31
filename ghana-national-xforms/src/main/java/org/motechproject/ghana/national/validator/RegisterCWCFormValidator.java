package org.motechproject.ghana.national.validator;

import org.motechproject.ghana.national.bean.RegisterCWCForm;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mobileforms.api.validator.FormValidator;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class RegisterCWCFormValidator extends FormValidator<RegisterCWCForm> {

    @Autowired
    private PatientService patientService;

    @Autowired
    private org.motechproject.ghana.national.validator.FormValidator formValidator;

    @Autowired
    private MobileMidwifeValidator mobileMidwifeValidator;

    @Override
    @LoginAsAdmin
    @ApiSession
    public List<FormError> validate(RegisterCWCForm formBean) {
        List<FormError> formErrors = super.validate(formBean);
        formErrors.addAll(formValidator.validateIfStaffExists(formBean.getStaffId()));
        formErrors.addAll(formValidator.validateIfFacilityExists(formBean.getFacilityId()));
        formErrors.addAll(validatePatient(formBean.getMotechId()));
        formErrors.addAll(validateMobileMidwifeIfEnrolled(formBean));
        return formErrors;
    }

    public List<FormError> validatePatient(String motechId) {
        List<FormError> patientErrors = formValidator.validatePatient(motechId, Constants.MOTECH_ID_ATTRIBUTE_NAME);
        return !patientErrors.isEmpty() ? patientErrors : formValidator.validateIfPatientIsAChild(motechId);
    }

    private List<FormError> validateMobileMidwifeIfEnrolled(RegisterCWCForm formBean) {
        MobileMidwifeEnrollment midwifeEnrollment = formBean.createMobileMidwifeEnrollment();
        return midwifeEnrollment != null ? mobileMidwifeValidator.validateForIncludeForm(midwifeEnrollment) : Collections.<FormError>emptyList();
    }
}
