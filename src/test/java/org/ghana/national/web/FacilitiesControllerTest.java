package org.ghana.national.web;

import org.apache.commons.lang.StringUtils;
import org.ghana.national.domain.Facility;
import org.ghana.national.exception.FacilityAlreadyFoundException;
import org.ghana.national.service.FacilityService;
import org.ghana.national.tools.Constants;
import org.ghana.national.web.form.CreateFacilityForm;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilitiesControllerTest {
    FacilitiesController facilitiesController;
    @Mock
    FacilityService mockFacilityService;
    @Mock
    MessageSource mockMessageSource;

    @Before
    public void setUp() {
        initMocks(this);
        facilitiesController = new FacilitiesController();
        ReflectionTestUtils.setField(facilitiesController, "facilityService", mockFacilityService);
        ReflectionTestUtils.setField(facilitiesController, "messageSource", mockMessageSource);
    }

    @Test
    public void shouldRenderForm() {
        when(mockFacilityService.facilities()).thenReturn(Arrays.asList(new Facility(new org.motechproject.mrs.model.Facility("facility"))));
        assertThat(facilitiesController.newFacilityForm(new ModelMap()), is(equalTo("facilities/new")));
    }

    @Test
    public void shouldSaveAFacilityWhenValid() {
        final BindingResult mockBindingResult = mock(BindingResult.class);
        final FacilitiesController spyFacilitiesController = spy(facilitiesController);
        ModelMap modelMap = new ModelMap();
        when(mockFacilityService.facilities()).thenReturn(Arrays.asList(new Facility()));
        final String result = spyFacilitiesController.createFacility(new CreateFacilityForm(), mockBindingResult, modelMap);

        assertThat(result, is(equalTo("facilities/success")));
        verify(spyFacilitiesController).populateFacilityData(anyListOf(Facility.class), eq(modelMap));
        assertNotNull(modelMap.get(Constants.CREATE_FACILITY_FORM));
        assertNotNull(modelMap.get(Constants.COUNTRIES));
        assertNotNull(modelMap.get(Constants.REGIONS));
        assertNotNull(modelMap.get(Constants.PROVINCES));
        assertNotNull(modelMap.get(Constants.DISTRICTS));
    }

    @Test
    public void testSaveFacilityWhenInValid() throws FacilityAlreadyFoundException {
        final BindingResult mockBindingResult = mock(BindingResult.class);
        final ModelMap modelMap = new ModelMap();
        final String facility = "facility";
        final String country = "country";
        final String region = "region";
        final String district = "district";
        final String province = "province";
        final String message = "Facility already exists.";
        final FacilitiesController spyFacilitiesController = spy(facilitiesController);
        doThrow(new FacilityAlreadyFoundException()).when(mockFacilityService).create(facility, country, region, district, province, StringUtils.EMPTY,StringUtils.EMPTY,StringUtils.EMPTY,StringUtils.EMPTY);
        when(mockMessageSource.getMessage("facility_already_exists", null, Locale.getDefault())).thenReturn(message);

        final CreateFacilityForm createFacilityForm = new CreateFacilityForm();
        createFacilityForm.setName(facility);
        createFacilityForm.setCountry(country);
        createFacilityForm.setRegion(region);
        createFacilityForm.setCountyDistrict(district);
        createFacilityForm.setStateProvince(province);
        createFacilityForm.setPhoneNumber(StringUtils.EMPTY);
        createFacilityForm.setAdditionalPhoneNumber1(StringUtils.EMPTY);
        createFacilityForm.setAdditionalPhoneNumber2(StringUtils.EMPTY);
        createFacilityForm.setAdditionalPhoneNumber3(StringUtils.EMPTY);
        final String result = spyFacilitiesController.createFacility(createFacilityForm, mockBindingResult, modelMap);

        final ArgumentCaptor<FieldError> captor = ArgumentCaptor.forClass(FieldError.class);
        verify(mockBindingResult).addError(captor.capture());
        final FieldError actualFieldError = captor.getValue();
        assertThat(result, is(equalTo("facilities/new")));
        assertThat(actualFieldError.getObjectName(), is(equalTo(Constants.CREATE_FACILITY_FORM)));
        assertThat(actualFieldError.getField(), is(equalTo("name")));
        assertThat(actualFieldError.getDefaultMessage(), is(equalTo(message)));
        verify(spyFacilitiesController).populateFacilityData(anyListOf(Facility.class), eq(modelMap));
        assertNotNull(modelMap.get(Constants.CREATE_FACILITY_FORM));
        assertNotNull(modelMap.get(Constants.COUNTRIES));
        assertNotNull(modelMap.get(Constants.REGIONS));
        assertNotNull(modelMap.get(Constants.PROVINCES));
        assertNotNull(modelMap.get(Constants.DISTRICTS));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPopulateFacilities() {
        List<Facility> facilities = populateData();
        ModelMap modelMap = facilitiesController.populateFacilityData(facilities, new ModelMap());
        assertThat((List<String>) modelMap.get("countries"), is(equalTo(Arrays.asList("Utopia"))));
        assertThat((Map<String, TreeSet<String>>) modelMap.get(Constants.REGIONS), is(equalTo(regions())));
        assertThat((Map<String, TreeSet<String>>) modelMap.get(Constants.DISTRICTS), is(equalTo(districts())));
        assertThat((Map<String, TreeSet<String>>) modelMap.get(Constants.PROVINCES), is(equalTo(provinces())));
    }

    static Map<String, TreeSet<String>> regions() {
        return new HashMap<String, TreeSet<String>>() {{
            put("Utopia", new TreeSet<String>() {{
                add("Region 1");
                add("Region 2");
                add("Region 3");
                add("Region 4");
            }});
        }};
    }

    static Map<String, TreeSet<String>> districts() {
        return new HashMap<String, TreeSet<String>>() {{
            put("Region 1", new TreeSet<String>() {{
                add("District 1");
                add("District 2");
            }});
            put("Region 2", new TreeSet<String>() {{
                add("District 4");
                add("District 3");
            }});
            put("Region 3", new TreeSet<String>() {{
                add("District 5");
                add("District 6");
                add("District 7");
            }});
            put("Region 4", new TreeSet<String>() {{
                add("null");
            }});
        }};
    }

    static Map<String, TreeSet<String>> provinces() {
        return new HashMap<String, TreeSet<String>>() {{
            put("District 1", new TreeSet<String>() {{
                add("Province 1");
                add("Province 2");
            }});
            put("District 4", new TreeSet<String>() {{
                add("Province 6");
            }});
            put("District 5", new TreeSet<String>() {{
                add("Province 7");
            }});
            put("District 2", new TreeSet<String>() {{
                add("Province 3");
            }});
            put("District 3", new TreeSet<String>() {{
                add("Province 4");
                add("Province 5");
            }});
            put("District 6", new TreeSet<String>() {{
                add("null");
            }});
            put("District 7", new TreeSet<String>() {{
                add("Province 8");
            }});
        }};
    }

    private List<Facility> populateData() {
        List<Facility> facilities = new ArrayList<Facility>();
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 1", "Utopia", "Region 1", "District 1", "Province 1")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 2", "Utopia", "Region 1", "District 1", "Province 2")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 3", "Utopia", "Region 1", "District 2", "Province 3")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 4", "Utopia", "Region 2", "District 3", "Province 4")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 5", "Utopia", "Region 2", "District 3", "Province 5")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 6", "Utopia", "Region 2", "District 4", "Province 6")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 7", "Utopia", "Region 3", "District 5", "Province 7")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 8", "Utopia", "Region 3", "District 7", "Province 8")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 9", "Utopia", "Region 3", "District 6", "null")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Facility 9", "Utopia", "Region 4", "null", "null")));
        facilities.add(new Facility(new org.motechproject.mrs.model.Facility("Unknown Location", "", "null", "null", "")));
        return facilities;
    }
}
