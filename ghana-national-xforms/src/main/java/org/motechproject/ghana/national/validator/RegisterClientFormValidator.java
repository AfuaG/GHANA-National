package org.motechproject.ghana.national.validator;

import org.motechproject.ghana.national.bean.RegisterClientForm;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.domain.RegistrationType;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mobileforms.api.validator.FormValidator;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.motechproject.ghana.national.domain.Constants.NOT_FOUND;

@Component
public class RegisterClientFormValidator extends FormValidator<RegisterClientForm> {

    @Autowired
    private PatientService patientService;
    @Autowired
    private org.motechproject.ghana.national.validator.FormValidator formValidator;
    @Autowired
    private RegisterCWCFormValidator cwcFormValidator;


    @Override
    @LoginAsAdmin
    @ApiSession
    public List<FormError> validate(RegisterClientForm formBean) {
        String motechId = formBean.getMotechId();
        List<FormError> formErrors = super.validate(formBean);
        formErrors.addAll(formValidator.validateIfStaffExists(formBean.getStaffId()));
        formErrors.addAll(formValidator.validateIfFacilityExists(formBean.getFacilityId()));
        formErrors.addAll(validateMotechId(motechId, formBean.getRegistrationMode()));
        formErrors.addAll(validateMotherMotechId(formBean.getMotherMotechId(), formBean.getRegistrantType()));
        if (PatientType.CHILD_UNDER_FIVE.equals(formBean.getRegistrantType())) {
            formErrors.addAll(cwcFormValidator.validatePatient(motechId));
        }
        return formErrors;
    }

    private List<FormError> validateMotechId(String motechId, RegistrationType registrationType) {
        if (RegistrationType.USE_PREPRINTED_ID.equals(registrationType) && patientService.getPatientByMotechId(motechId) != null) {
            return new ArrayList<FormError>() {{
                add(new FormError("motechId", "in use"));
            }};
        }
        return new ArrayList<FormError>();
    }

    private List<FormError> validateMotherMotechId(String motherMotechId, PatientType patientType) {
        if (PatientType.CHILD_UNDER_FIVE.equals(patientType)) {
            if (motherMotechId == null || patientService.getPatientByMotechId(motherMotechId) == null) {
                return new ArrayList<FormError>() {{
                    add(new FormError("motherMotechId", NOT_FOUND));
                }};
            }
        }
        return new ArrayList<FormError>();
    }
}
