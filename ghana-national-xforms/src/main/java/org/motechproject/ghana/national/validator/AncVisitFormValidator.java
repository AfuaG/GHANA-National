package org.motechproject.ghana.national.validator;

import org.motechproject.ghana.national.bean.ANCVisitForm;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.mobileforms.api.validator.FormValidator;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.motechproject.ghana.national.domain.EncounterType.ANC_REG_VISIT;

@Component
public class AncVisitFormValidator extends FormValidator<ANCVisitForm> {

    @Autowired
    private org.motechproject.ghana.national.validator.FormValidator formValidator;

    @Autowired
    private AllEncounters allEncounters;

    @Override
    @LoginAsAdmin
    @ApiSession
    public List<FormError> validate(ANCVisitForm formBean) {
        List<FormError> errors = super.validate(formBean);
        return businessValidations(formBean, errors);
    }

    List<FormError> businessValidations(ANCVisitForm formBean, List<FormError> errors) {
        errors.addAll(formValidator.validateIfStaffExists(formBean.getStaffId()));
        errors.addAll(formValidator.validateIfFacilityExists(formBean.getFacilityId()));
        errors.addAll(formValidator.validateIfPatientExistsAndIsAlive(formBean.getMotechId(), Constants.MOTECH_ID_ATTRIBUTE_NAME));
        errors.addAll(formValidator.validateIfPatientIsFemale(formBean.getMotechId(), Constants.MOTECH_ID_ATTRIBUTE_NAME));
        errors.addAll(validateIfPatientAlreadyEnrolledForANC(formBean.getMotechId(), ANC_REG_VISIT.value()));
        return errors;
    }

    private ArrayList<FormError> validateIfPatientAlreadyEnrolledForANC(final String motechId, String encounterAncregvisit) {
        if (allEncounters.getLatest(motechId, encounterAncregvisit) == null) {
            return new ArrayList<FormError>() {{
                add(new FormError(motechId, "NOT REGISTERED FOR ANC"));
            }};
        }
        return new ArrayList<FormError>();
    }
}
