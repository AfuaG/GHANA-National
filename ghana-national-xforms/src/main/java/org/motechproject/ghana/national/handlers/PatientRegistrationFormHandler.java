package org.motechproject.ghana.national.handlers;

import org.motechproject.ghana.national.bean.RegisterClientForm;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientAttributes;
import org.motechproject.ghana.national.domain.PatientType;
import org.motechproject.ghana.national.domain.RegistrationToday;
import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
import org.motechproject.ghana.national.service.*;
import org.motechproject.ghana.national.tools.Utility;
import org.motechproject.ghana.national.vo.ANCVO;
import org.motechproject.ghana.national.vo.CwcVO;
import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
import org.motechproject.model.MotechEvent;
import org.motechproject.mrs.model.Attribute;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.motechproject.server.event.annotations.MotechListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class PatientRegistrationFormHandler implements FormPublishHandler {

    public static final String FORM_BEAN = "formBean";

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PatientService patientService;

    @Autowired
    private CareService careService;

    @Autowired
    private MobileMidwifeService mobileMidwifeService;

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private TextMessageService textMessageService;

    @Override
    @MotechListener(subjects = "form.validation.successful.NurseDataEntry.registerPatient")
    @LoginAsAdmin
    @ApiSession
    public void handleFormEvent(MotechEvent event) {
        try {
            RegisterClientForm registerClientForm = (RegisterClientForm) event.getParameters().get(FORM_BEAN);

            MRSPerson mrsPerson = new MRSPerson().
                    firstName(registerClientForm.getFirstName()).
                    middleName(registerClientForm.getMiddleName()).
                    lastName(registerClientForm.getLastName()).
                    dateOfBirth(registerClientForm.getDateOfBirth()).
                    birthDateEstimated(registerClientForm.getEstimatedBirthDate()).
                    gender(registerClientForm.getSex()).
                    address(registerClientForm.getAddress()).
                    attributes(getPatientAttributes(registerClientForm));

            String facilityId = facilityService.getFacilityByMotechId(registerClientForm.getFacilityId()).mrsFacilityId();
            MRSPatient mrsPatient = new MRSPatient(registerClientForm.getMotechId(), mrsPerson, new MRSFacility(facilityId));

            Patient patient = new Patient(mrsPatient, registerClientForm.getMotherMotechId());
            String patientMotechId = patientService.registerPatient(patient);

            if (registerClientForm.isEnrolledForMobileMidwifeProgram()) {
                MobileMidwifeEnrollment mobileMidwifeEnrollment = registerClientForm.createMobileMidwifeEnrollment(patientMotechId);
                mobileMidwifeEnrollment.setFacilityId(facilityId);
                mobileMidwifeService.register(mobileMidwifeEnrollment);
            }

            if (registerClientForm.getRegistrantType().equals(PatientType.CHILD_UNDER_FIVE)) {
                CwcVO cwcVO = new CwcVO(registerClientForm.getStaffId(), facilityId, registerClientForm.getDate(),
                        patientMotechId, registerClientForm.getCWCCareHistories(), registerClientForm.getBcgDate(), registerClientForm.getLastVitaminADate(), registerClientForm.getMeaslesDate(),
                        registerClientForm.getYellowFeverDate(), registerClientForm.getLastPentaDate(), registerClientForm.getLastPenta(), registerClientForm.getLastOPVDate(),
                        registerClientForm.getLastOPV(), registerClientForm.getLastIPTiDate(), registerClientForm.getLastIPTi(), registerClientForm.getCwcRegNumber(), registerClientForm.getAddHistory());

                careService.enroll(cwcVO);

            }

            if (PatientType.PREGNANT_MOTHER.equals(registerClientForm.getRegistrantType())) {
                ANCVO ancVO = new ANCVO(registerClientForm.getStaffId(), facilityId, patientMotechId, registerClientForm.getDate()
                        , RegistrationToday.TODAY, registerClientForm.getAncRegNumber(), registerClientForm.getExpDeliveryDate(), registerClientForm.getHeight(), registerClientForm.getGravida(),
                        registerClientForm.getParity(), registerClientForm.getAddHistory(), registerClientForm.getDeliveryDateConfirmed(), registerClientForm.getAncCareHistories(), registerClientForm.getLastIPT(), registerClientForm.getLastTT(),
                        registerClientForm.getLastIPTDate(), registerClientForm.getLastTTDate(), registerClientForm.getAddHistory());

                careService.enroll(ancVO);

            }
            if (registerClientForm.getSender() != null) {
                textMessageService.sendSMS(registerClientForm.getSender(), patientMotechId, patient, "REGISTER_SUCCESS_SMS");
            }
        } catch (Exception e)

        {
            log.error("Exception while saving patient", e);
        }

    }

    private List<Attribute> getPatientAttributes
            (RegisterClientForm
                     registerClientForm) {
        List<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute(PatientAttributes.PHONE_NUMBER.getAttribute(), registerClientForm.getPhoneNumber()));
        attributes.add(new Attribute(PatientAttributes.NHIS_EXPIRY_DATE.getAttribute(), Utility.safeToString(registerClientForm.getNhisExpires())));
        attributes.add(new Attribute(PatientAttributes.NHIS_NUMBER.getAttribute(), registerClientForm.getNhis()));
        attributes.add(new Attribute(PatientAttributes.INSURED.getAttribute(), Utility.safeToString(registerClientForm.getInsured())));
        return attributes;
    }
}
