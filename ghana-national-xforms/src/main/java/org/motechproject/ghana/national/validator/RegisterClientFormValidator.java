package org.motechproject.ghana.national.validator;

import org.apache.commons.collections.CollectionUtils;
import org.motechproject.ghana.national.bean.RegisterClientForm;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.domain.RegistrationType;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.ghana.national.service.UserService;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mobileforms.api.validator.FormValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RegisterClientFormValidator extends FormValidator<RegisterClientForm> {

    public static final String NOT_FOUND = "not found";
    @Autowired
    private UserService userService;
    @Autowired
    private FacilityService facilityService;
    @Autowired
    private PatientService patientService;


    @Override
    public List<FormError> validate(RegisterClientForm formBean) {
        List<FormError> formErrors = super.validate(formBean);
        validateIfStaffExists(formErrors, formBean.getStaffId());
        validateIfFacilityExists(formErrors, formBean.getFacilityId());
        validateIfMotechId(formErrors, formBean.getMotechId(), formBean.getRegistrationMode());
        validateIfMotherMotechId(formErrors, formBean.getMotherMotechId(), formBean.getRegistrantType());
        return formErrors;
    }

    private void validateIfMotechId(List<FormError> formErrors, String motechId, RegistrationType registrationType) {
        if (RegistrationType.USE_PREPRINTED_ID.equals(registrationType) && patientService.getPatientById(motechId) != null)
            CollectionUtils.addIgnoreNull(formErrors, new FormError("motechId", "in use"));
    }

    private void validateIfMotherMotechId(List<FormError> formErrors, String motherMotechId, PatientType patientType) {
        if (PatientType.CHILD_UNDER_FIVE.equals(patientType)) {
            if (motherMotechId == null || patientService.getPatientById(motherMotechId) == null) {
                CollectionUtils.addIgnoreNull(formErrors, new FormError("motherMotechId", NOT_FOUND));
            }
        }
    }

    private void validateIfStaffExists(List<FormError> formErrors, final String staffId) {
        if (userService.getUserById(staffId) == null)
            CollectionUtils.addIgnoreNull(formErrors, new FormError("staffId", NOT_FOUND));
    }

    private void validateIfFacilityExists(List<FormError> formErrors, String facilityId) {
        if (facilityService.getFacility(facilityId) == null)
            CollectionUtils.addIgnoreNull(formErrors, new FormError("facilityId", NOT_FOUND));
    }


}
