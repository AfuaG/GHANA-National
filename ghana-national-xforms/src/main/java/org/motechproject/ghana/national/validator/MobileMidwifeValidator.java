package org.motechproject.ghana.national.validator;

import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.model.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.motechproject.ghana.national.domain.Constants.*;
import static org.motechproject.ghana.national.domain.mobilemidwife.PhoneOwnership.PUBLIC;

@Component
public class MobileMidwifeValidator {

    @Autowired
    private org.motechproject.ghana.national.validator.FormValidator formValidator;
    final static String PATIENT_ID_ATTRIBUTE_NAME = "patientId";

    public List<FormError> validate(MobileMidwifeEnrollment enrollment) {
        List<FormError> formErrors = new ArrayList<FormError>();
        formErrors.addAll(validateFacilityPatientAndStaff(enrollment));
        formErrors.addAll(validateFieldValues(enrollment));
        return formErrors;
    }

    public List<FormError> validateFieldValues(MobileMidwifeEnrollment enrollment) {
        List<FormError> formErrors = new ArrayList<FormError>();
        formErrors.addAll(validateTime(enrollment));
        return formErrors;
    }

    public List<FormError> validateForIncludeForm(MobileMidwifeEnrollment enrollment) {
        List<FormError> formErrors = new ArrayList<FormError>();
        formErrors.addAll(validateFieldValues(enrollment));
        return formErrors;
    }

    private List<FormError> validateFacilityPatientAndStaff(MobileMidwifeEnrollment enrollment) {
        return validateFacilityPatientAndStaff(enrollment.getPatientId(), enrollment.getFacilityId(), enrollment.getStaffId());
    }

    public List<FormError> validateFacilityPatientAndStaff(String patientId, String facilityId, String staffId) {
        List<FormError> formErrors = new ArrayList<FormError>();
        formErrors.addAll(formValidator.validatePatient(patientId, PATIENT_ID_ATTRIBUTE_NAME));
        formErrors.addAll(formValidator.validateIfFacilityExists(facilityId));
        formErrors.addAll(formValidator.validateIfStaffExists(staffId));
        return formErrors;
    }

    List<FormError> validateTime(MobileMidwifeEnrollment enrollment) {

        if (Medium.VOICE.equals(enrollment.getMedium()) && !PUBLIC.equals(enrollment.getPhoneOwnership())) {
            Time timeOfDay = enrollment.getTimeOfDay();
            if (timeOfDay == null) {
                    return asList(new FormError("", MOBILE_MIDWIFE_VOICE_TIMEOFDAYREQUIRED_MESSAGE));
            } else if (!withinValidTimeRange(timeOfDay))
                return asList(new FormError("", MOBILE_MIDWIFE_VOICE_TIMEOFDAYRANGE_MESSAGE));
        }
        return emptyList();
    }

    private boolean withinValidTimeRange(Time timeOfDay) {
        Time maxTime = MOBILE_MIDWIFE_MAX_TIMEOFDAY_FOR_VOICE;
        Time minTime = MOBILE_MIDWIFE_MIN_TIMEOFDAY_FOR_VOICE;
        return (timeOfDay.ge(minTime) && timeOfDay.le(maxTime));
    }


}
