package org.motechproject.ghana.national.web;

import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.RegistrationToday;
import org.motechproject.ghana.national.service.CWCService;
import org.motechproject.ghana.national.service.PatientService;
import org.motechproject.ghana.national.validator.RegisterCWCFormValidator;
import org.motechproject.ghana.national.vo.CwcVO;
import org.motechproject.ghana.national.web.form.CWCEnrollmentForm;
import org.motechproject.ghana.national.web.helper.CwcFormMapper;
import org.motechproject.ghana.national.web.helper.FacilityHelper;
import org.motechproject.mobileforms.api.domain.FormError;
import org.motechproject.openmrs.advice.ApiSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("admin/cwc")
public class CWCController {


    @Autowired
    PatientService patientService;

    @Autowired
    CWCService cwcService;

    @Autowired
    FacilityHelper facilityHelper;

    @Autowired
    CwcFormMapper cwcFormMapper;

    @Autowired
    RegisterCWCFormValidator registerCWCFormValidator;

    private String error = "error";
    public static final String ENROLLMENT_CWC_FORM = "cwcEnrollmentForm";
    public static final String ENROLL_CWC_URL = "cwc/new";
    public static final String PATIENT_NOT_FOUND = "Patient Not Found";
    public static final String PATIENT_IS_NOT_A_CHILD = "Patient is Not A Child";
    public static final String REGISTRATION_OPTIONS = "registrationOptions";

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

    @ApiSession
    @RequestMapping(value = "/new", method = RequestMethod.GET)
    public String create(@RequestParam String motechPatientId, ModelMap modelMap) {
        Patient patient = patientService.getPatientByMotechId(motechPatientId);
        modelMap.addAttribute(ENROLLMENT_CWC_FORM, cwcFormMapper.mapEncounterToView(cwcService.getEncounter(motechPatientId)));
        modelMap.mergeAttributes(cwcFormMapper.setViewAttributes());
        modelMap.mergeAttributes(facilityHelper.locationMap());

        if (patient == null) {
            modelMap.addAttribute(error, PATIENT_NOT_FOUND);
            return ENROLL_CWC_URL;
        }

        if (patientService.getAgeOfPatientByMotechId(motechPatientId) >= 5) {
            modelMap.addAttribute(error, PATIENT_IS_NOT_A_CHILD);
            return ENROLL_CWC_URL;
        }
        return ENROLL_CWC_URL;
    }

    @ApiSession
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String save(@Valid CWCEnrollmentForm cwcEnrollmentForm, ModelMap modelMap) {
        modelMap.addAttribute(ENROLLMENT_CWC_FORM, cwcEnrollmentForm);
        modelMap.mergeAttributes(cwcFormMapper.setViewAttributes());
        modelMap.mergeAttributes(facilityHelper.locationMap());

        List<FormError> formErrors = registerCWCFormValidator.validate(cwcEnrollmentForm.getPatientMotechId(), 
                cwcEnrollmentForm.getStaffId(), cwcEnrollmentForm.getFacilityForm().getFacilityId());

        String validationErrors = "";
        for (FormError formError : formErrors) {
            validationErrors += formError.getParameter() + " " + formError.getError();
            validationErrors += "\n";
        }
        
        if(!validationErrors.isEmpty()) {
            modelMap.addAttribute(error, validationErrors);
            return ENROLL_CWC_URL;
        }

        Date registrationDate = cwcEnrollmentForm.getRegistrationDate();
        if (cwcEnrollmentForm.getRegistrationToday().equals(RegistrationToday.TODAY)) {
            registrationDate = new Date();
        }
        cwcService.enroll(new CwcVO(
                cwcEnrollmentForm.getStaffId(),
                cwcEnrollmentForm.getFacilityForm().getFacilityId(),
                registrationDate,
                cwcEnrollmentForm.getPatientMotechId(),
                cwcEnrollmentForm.getBcgDate(),
                cwcEnrollmentForm.getVitADate(),
                cwcEnrollmentForm.getMeaslesDate(),
                cwcEnrollmentForm.getYfDate(),
                cwcEnrollmentForm.getLastPentaDate(),
                cwcEnrollmentForm.getLastPenta(),
                cwcEnrollmentForm.getLastOPVDate(),
                cwcEnrollmentForm.getLastOPV(),
                cwcEnrollmentForm.getLastIPTiDate(),
                cwcEnrollmentForm.getLastIPTi(),
                cwcEnrollmentForm.getSerialNumber()));
        modelMap.addAttribute("success", "Client registered for CWC successfully.");
        return ENROLL_CWC_URL;
    }
}
