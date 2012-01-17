package org.motechproject.ghana.national.functional.Generator;

import org.motechproject.ghana.national.web.FacilityController;
import org.motechproject.ghana.national.web.form.FacilityForm;
import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;
import org.springframework.beans.factory.annotation.Autowired;

import org.motechproject.openmrs.advice.ApiSession;
import org.motechproject.openmrs.advice.LoginAsAdmin;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;

import static org.mockito.Mockito.mock;

@Component
public class FacilityGenerator {

    @Autowired
    FacilityController facilityController;

    public FacilityGenerator() {
    }

    public FacilityGenerator(FacilityController facilityController) {
        this.facilityController = facilityController;
    }

    @LoginAsAdmin
    @ApiSession
    @Transactional(readOnly = true)
    public String createDummyFacilityAndReturnFacilityId(){
        FacilityForm facilityForm = createFacilityForm();
        BindingResult mockBindingResult = mock(BindingResult.class);
        ModelMap modelMap = new ModelMap();
        facilityController.create(facilityForm,mockBindingResult,modelMap);
        FacilityForm createdFacility = (FacilityForm) modelMap.get(FacilityController.FACILITY_FORM);
        return createdFacility.getFacilityId();
    }

    private FacilityForm createFacilityForm() {
        FacilityForm facilityForm = new FacilityForm();
        String name = "Dummy Facility";
        String country = "Ghana";
        String region = "Western Ghana";
        String province = "Province";
        String district = "District";
        String phoneNumber = "0123456780";
        String addPhoneNumber1 = "0123456788";
        String addPhoneNumber2 = "0123456787";
        String addPhoneNumber3 = "0123456784";
        facilityForm.setName(name);
        facilityForm.setCountry(country);
        facilityForm.setRegion(region);
        facilityForm.setCountyDistrict(district);
        facilityForm.setStateProvince(province);
        facilityForm.setPhoneNumber(phoneNumber);
        facilityForm.setAdditionalPhoneNumber1(addPhoneNumber1);
        facilityForm.setAdditionalPhoneNumber2(addPhoneNumber2);
        facilityForm.setAdditionalPhoneNumber3(addPhoneNumber3);
        return facilityForm;
    }
}
