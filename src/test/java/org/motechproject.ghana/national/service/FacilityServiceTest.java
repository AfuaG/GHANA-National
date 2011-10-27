package org.motechproject.ghana.national.service;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.exception.FacilityAlreadyFoundException;
import org.motechproject.ghana.national.repository.AllFacilities;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class FacilityServiceTest {

    FacilityService facilityService;
    @Mock
    private AllFacilities mockAllFacilities;

    @Before
    public void init() {
        initMocks(this);
        facilityService = new FacilityService(mockAllFacilities);
        ReflectionTestUtils.setField(facilityService, "allFacilities", mockAllFacilities);
    }

    @Test
    public void shouldCreateAFacility() throws FacilityAlreadyFoundException {
        String facilityName = "name";
        String country = "country";
        String region = "region";
        String district = "district";
        String province = "province";
        String phoneNumber = "1";
        String additionalPhoneNumber1 = "2";
        String additionalPhoneNumber2 = "3";
        String additionalPhoneNumber3 = "4";
        when(mockAllFacilities.facilitiesByName(facilityName)).thenReturn(Collections.<Facility>emptyList());
        facilityService.create(facilityName, country, region, district, province, phoneNumber, additionalPhoneNumber1, additionalPhoneNumber2, additionalPhoneNumber3);
        final ArgumentCaptor<Facility> captor = ArgumentCaptor.forClass(Facility.class);
        verify(mockAllFacilities).add(captor.capture());

        final Facility savedFacility = captor.getValue();
        assertThat(savedFacility.name(), is(equalTo(facilityName)));
        assertThat(savedFacility.region(), is(equalTo(region)));
        assertThat(savedFacility.district(), is(equalTo(district)));
        assertThat(savedFacility.province(), is(equalTo(province)));
        assertThat(savedFacility.country(), is(equalTo(country)));
        assertThat(savedFacility.phoneNumber(), is(equalTo(phoneNumber)));
        assertThat(savedFacility.additionalPhoneNumber1(), is(equalTo(additionalPhoneNumber1)));
        assertThat(savedFacility.additionalPhoneNumber2(), is(equalTo(additionalPhoneNumber2)));
        assertThat(savedFacility.additionalPhoneNumber3(), is(equalTo(additionalPhoneNumber3)));
    }

    @Test(expected = FacilityAlreadyFoundException.class)
    public void ShouldNotCreateAFacilityWhenAlreadyExists() throws FacilityAlreadyFoundException {
        String facilityName = "name";
        String country = "country";
        String region = "region";
        String district = "district";
        String province = "province";
        when(mockAllFacilities.facilitiesByName(facilityName)).thenReturn(Arrays.asList(new Facility(new org.motechproject.mrs.model.Facility(facilityName, country, region, district, province))));
        facilityService.create(facilityName, country, region, district, province, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY);
    }

    @Test
    public void shouldGetAllFacilities() {
        facilityService.facilities();
        verify(mockAllFacilities).facilities();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPopulateFacilities() {
        List<Facility> facilities = populateData();
        when(mockAllFacilities.facilities()).thenReturn(facilities);
        Map modelMap = facilityService.populateFacilityData();
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

