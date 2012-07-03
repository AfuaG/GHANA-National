package org.motechproject.ghana.national.repository;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ghana.national.BaseIntegrationTest;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.services.MRSFacilityAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class AllFacilitiesTest extends BaseIntegrationTest {

    @Mock
    MRSFacilityAdapter mockMrsFacilityAdapter;
    @Mock
    AllMotechModuleFacilities mockAllMotechModuleFacilities;

    @Value("#{couchdbProperties['host']}")
    private String host;

    @Value("#{couchdbProperties['port']}")
    private String port;

    @Autowired
    private AllFacilities allFacilities;

    @Before
    public void init() {
        initMocks(this);
        ReflectionTestUtils.setField(allFacilities, "facilityAdapter", mockMrsFacilityAdapter);
        ReflectionTestUtils.setField(allFacilities, "allMotechModuleFacilities", mockAllMotechModuleFacilities);
    }

    @Test
    public void shouldSaveAFacility() throws Exception {
        final String facilityName = "name";
        final String phoneNumber = "0123456789";
        String additionalPhone1 = "12345";
        String additionalPhone2 = "321234";
        String additionalPhone3 = "33344";
        final MRSFacility mrsFacility = new MRSFacility(facilityName);
        Facility facility = facility(phoneNumber, additionalPhone1, additionalPhone2, additionalPhone3, mrsFacility);
        final int initialSize = allFacilities.getAll().size();

        final String facilityId = "12";
        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(new MRSFacility(facilityId, facilityName,
                "country", "region", "district", "province"));

        allFacilities.add(facility);

        final List<Facility> facilities = allFacilities.getAll();
        final int actualSize = facilities.size();
        final Facility actualFacility = facilities.iterator().next();
        assertThat(actualSize, is(equalTo(initialSize + 1)));
        assertThat(actualFacility.phoneNumber(), is(equalTo(phoneNumber)));
        assertThat(actualFacility.additionalPhoneNumber1(), is(equalTo(additionalPhone1)));
        assertThat(actualFacility.additionalPhoneNumber2(), is(equalTo(additionalPhone2)));
        assertThat(actualFacility.additionalPhoneNumber3(), is(equalTo(additionalPhone3)));
        assertThat(actualFacility.mrsFacilityId(), is(equalTo(facilityId)));
        verify(mockAllMotechModuleFacilities).save(facility);
    }

    @Test
    public void shouldBeIdempotentOnDuplicateFacilityCreation() throws IOException, JSONException {

        final String facilityName = "Xyz facility";
        final MRSFacility mrsFacility = new MRSFacility(facilityName);
        final String facilityId = "12";
        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(new MRSFacility(facilityId, facilityName,
                "country", "region", "district", "province"));

        final int initialSize = allFacilities.getAll().size();

        allFacilities.add(facility("950001", "123245", "23423243", "2342342342", mrsFacility));
        assertThat(allFacilities.getAll().size(), is(initialSize + 1));

        allFacilities.add(facility("950002", "123245", "23423243", "2342342342", mrsFacility));
        assertThat(allFacilities.getAll().size(), is(initialSize + 1));
    }

    @Test
    public void shouldGetAllFacilitiesByName() {
        final String facilityName = "name";
        final String country = "country";
        final String region = "region";
        final String district = "district";
        final String province = "province";
        final String phoneNumber = "0123456789";
        final String mrsFacilityId = "12";

        final MRSFacility mrsFacility = new MRSFacility(mrsFacilityId,
                facilityName, country, region, district, province);
        Facility facility = new Facility(mrsFacility)
                .phoneNumber(phoneNumber);

        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(new MRSFacility(mrsFacilityId, facilityName,
                "country", "region", "district", "province"));
        allFacilities.add(facility);
        when(mockMrsFacilityAdapter.getFacilities(facilityName)).thenReturn(Arrays.asList(mrsFacility));

        final List<Facility> actualFacilities = allFacilities.facilitiesByName(facilityName);

        final Facility actualFacility = actualFacilities.iterator().next();
        assertFacility(actualFacility, mrsFacilityId, facilityName, country, region, district, province);
        assertThat(actualFacility.phoneNumber(), is(equalTo(phoneNumber)));
    }

    @Test
    public void shouldReturnListOfFacilitiesByNameEvenIfTheRecordsAreMissingInCouchDb() {
        final String facilityName = "name";
        final String country = "country";
        final String region = "region";
        final String district = "district";
        final String province = "province";
        final String mrsFacilityId = "13";

        final MRSFacility mrsFacility = new MRSFacility(mrsFacilityId,
                facilityName, country, region, district, province);
        when(mockMrsFacilityAdapter.getFacilities(facilityName)).thenReturn(Arrays.asList(mrsFacility));

        final List<Facility> actualFacilities = allFacilities.facilitiesByName(facilityName);

        final Facility actualFacility = actualFacilities.iterator().next();
        assertThat(actualFacility.name(), is(equalTo(facilityName)));
        assertThat(actualFacility.country(), is(equalTo(country)));
        assertThat(actualFacility.region(), is(equalTo(region)));
        assertThat(actualFacility.province(), is(equalTo(province)));
        assertThat(actualFacility.district(), is(equalTo(district)));
    }

    @Test
    public void shouldGetAllFacilities() {
        final String mrsFacilityId = "12345";
        final String facilityName = "name";
        final String country = "Utopia";
        final String region = "Region 1";
        final String district = "District 1";
        final String province = "Province 1";
        final MRSFacility facility = new MRSFacility(mrsFacilityId, facilityName, country, region, district, province);

        when(mockMrsFacilityAdapter.saveFacility(facility)).thenReturn(new MRSFacility(mrsFacilityId, facilityName,
                country, region, district, province));
        allFacilities.add(new Facility(facility));

        when(mockMrsFacilityAdapter.getFacilities()).thenReturn(Arrays.asList(facility));
        final List<Facility> actualFacilities = allFacilities.facilities();
        final Facility actualFacility = actualFacilities.iterator().next();
        assertThat(actualFacilities.size(), is(equalTo(1)));
        assertFacility(actualFacility, mrsFacilityId, facilityName, country, region, district, province);
    }

    private void assertFacility(Facility actualFacility, String facilityId, String facilityName, String country, String region, String district, String province) {
        assertThat(actualFacility.name(), is(equalTo(facilityName)));
        assertThat(actualFacility.country(), is(equalTo(country)));
        assertThat(actualFacility.region(), is(equalTo(region)));
        assertThat(actualFacility.province(), is(equalTo(province)));
        assertThat(actualFacility.district(), is(equalTo(district)));
        assertThat(actualFacility.mrsFacility().getId(), is(equalTo(facilityId)));
    }

    @Test
    public void shouldFetchFacilitiesGivenAListOfMRSFacilityIds() {
        final MRSFacility anyFacility1 = new MRSFacility("1", "name", "country", "region", "district", "province");
        final MRSFacility anyFacility2 = new MRSFacility("2", "name", "country", "region", "district", "province");
        final MRSFacility anyFacility3 = new MRSFacility("3", "name", "country", "region", "district", "province");
        final MRSFacility anyFacility4 = new MRSFacility("4", "name", "country", "region", "district", "province");
        final Facility facility1 = new Facility(new MRSFacility("1", "name1", "country", "region", "district", "province"));
        final Facility facility2 = new Facility(new MRSFacility("2", "name2", "country", "region", "district", "province"));
        final Facility facility3 = new Facility(new MRSFacility("3", "name3", "country", "region", "district", "province"));
        final Facility facility4 = new Facility(new MRSFacility("4", "name4", "country", "region", "district", "province"));
        when(mockMrsFacilityAdapter.saveFacility(facility1.mrsFacility())).thenReturn(anyFacility1);
        when(mockMrsFacilityAdapter.saveFacility(facility2.mrsFacility())).thenReturn(anyFacility2);
        when(mockMrsFacilityAdapter.saveFacility(facility3.mrsFacility())).thenReturn(anyFacility3);
        when(mockMrsFacilityAdapter.saveFacility(facility4.mrsFacility())).thenReturn(anyFacility4);
        allFacilities.add(facility1);
        allFacilities.add(facility2);
        allFacilities.add(facility3);
        allFacilities.add(facility4);

        final List<Facility> byFacilityIds = allFacilities.findByFacilityIds(Arrays.asList("1", "2"));
        assertThat(byFacilityIds.size(), is(2));
    }

    @Test
    public void shouldGetFacilityByMrsFacilityId() {
        String facilityId = "0987654";
        String name = "name";
        String province = "province";
        String district = "district";
        String region = "region";
        String country = "country";

        final MRSFacility mrsFacility = new MRSFacility(facilityId, name, country, region, district, province);

        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(mrsFacility);
        allFacilities.add(facility("123460987", null, null, null, mrsFacility).motechId(facilityId));

        when(mockMrsFacilityAdapter.getFacility(facilityId)).thenReturn(mrsFacility);

        Facility facility;
        assertFacility(allFacilities.getFacility(facilityId), facilityId, name, country, region, district, province);

        when(mockMrsFacilityAdapter.getFacility(facilityId)).thenReturn(null);
        facility = allFacilities.getFacility(facilityId);
        assertThat(facility, is(equalTo(null)));
    }

    @Test
    public void shouldGetFacilityByMotechFacilityId() {
        AllFacilities allFacilitiesSpy = spy(allFacilities);
        String motechFacilityId = "10000";
        String mrsFacilityId = "10";
        String phone_number = "0123456789";
        Facility motechFacility = facility(phone_number, null, null, null, null).mrsFacilityId(mrsFacilityId).motechId(motechFacilityId);
        MRSFacility mrsFacility = mock(MRSFacility.class);

        doReturn(motechFacility).when(allFacilitiesSpy).findByMotechFacilityId(motechFacilityId);
        when(mockMrsFacilityAdapter.getFacility(mrsFacilityId)).thenReturn(mrsFacility);

        Facility returnedFacility = allFacilitiesSpy.getFacilityByMotechId(motechFacilityId);
        assertThat(returnedFacility.motechId(), is(equalTo(motechFacilityId)));
        assertThat(returnedFacility.mrsFacilityId(), is(equalTo(mrsFacilityId)));
        assertThat(returnedFacility.phoneNumber(), is(equalTo(phone_number)));
        assertThat(returnedFacility.mrsFacility(), is(equalTo(mrsFacility)));
    }

    @Test
    public void shouldFindFacilityById() {
        String mrsFacilityId = "1234";
        String country = "Country";
        MRSFacility mrsFacility = new MRSFacility(mrsFacilityId, "facilityName", country, "Region", null, null);
        String phoneNumber = "9911002288";
        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(mrsFacility);
        allFacilities.add(facility(phoneNumber, null, null, null, mrsFacility));

        assertThat(allFacilities.findByMrsFacilityId(mrsFacilityId).phoneNumber(), is(phoneNumber));
    }

    @Test
    public void shouldReplaceFacilityIfAlreadyPresent() {
        String mrsFacilityId = "1234";
        String country = "Country";
        MRSFacility mrsFacility = new MRSFacility(mrsFacilityId, "facilityName", country, "Region", null, null);
        String phoneNumber = "9911002288";
        String newPhoneNumber= "8911002288";
        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(mrsFacility);
        allFacilities.add(facility(phoneNumber, null, null, null, mrsFacility));
        allFacilities.addOrReplace(facility(newPhoneNumber, null, null, null, mrsFacility));

        assertThat(allFacilities.findByMrsFacilityId(mrsFacilityId).phoneNumber(), is(newPhoneNumber));
    }

    @Test
    public void shouldFindFacilityByMotechId() {
        String motechFacilityId = "1346789";
        String mrsFacilityId = "100";
        MRSFacility mrsFacility = new MRSFacility(mrsFacilityId);
        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(mrsFacility);
        allFacilities.add(facility(
                "9911002288", null, null, null, mrsFacility).motechId(motechFacilityId));

        assertThat(allFacilities.findByMotechFacilityId(motechFacilityId).motechId(), is(motechFacilityId));
        assertThat(allFacilities.findByMotechFacilityId(motechFacilityId).mrsFacility(), is(equalTo(null)));
    }

    private Facility facility(String phoneNumber, String additionalPhone1, String additionalPhone2, String additionalPhone3, MRSFacility mrsFacility) {
        return new Facility(mrsFacility)
                .phoneNumber(phoneNumber).additionalPhoneNumber1(additionalPhone1).additionalPhoneNumber2(additionalPhone2)
                .additionalPhoneNumber3(additionalPhone3);
    }

    @Test
    public void shouldUpdateFacility() {
        final String facilityId = "16";
        final String facilityName = "name";
        final String phoneNumber = "0123456789";
        String additionalPhone1 = "12345";
        String additionalPhone2 = "321234";
        String additionalPhone3 = "33344";

        final String updatedFacilityName = "name";
        final String updatedPhoneNumber = "0123456789";
        String updatedAdditionalPhone1 = "12345";
        String updatedAdditionalPhone2 = "321234";
        String updatedAdditionalPhone3 = "33344";

        final MRSFacility mrsFacility = new MRSFacility(facilityName);
        Facility facility = facility(phoneNumber, additionalPhone1, additionalPhone2, additionalPhone3, mrsFacility);

        when(mockMrsFacilityAdapter.saveFacility(mrsFacility)).thenReturn(new MRSFacility(facilityId, facilityName,
                "country", "region", "district", "province"));
        allFacilities.add(facility);

        final MRSFacility updatedMRSFacility = new MRSFacility(updatedFacilityName);
        facility.phoneNumber(updatedPhoneNumber)
                .additionalPhoneNumber1(updatedAdditionalPhone1)
                .additionalPhoneNumber2(updatedAdditionalPhone2)
                .additionalPhoneNumber3(updatedAdditionalPhone3)
                .mrsFacility(updatedMRSFacility);

        allFacilities.update(facility);
        verify(mockAllMotechModuleFacilities).update(facility);
    }

    @Test
    public void shouldFetchAllPhoneNumbersOfAllFacilities() {
        String phoneNumber1 = "1234";
        String phoneNumber2 = "2345";
        String additionalPhoneNumber1 = "5567";
        String additionalPhoneNumber2 = "2323";
        String phoneNumber4 = "4567";
        String phoneNumber3 = "3456";
        String facilityName1 = "name1";
        String facilityName2 = "name2";
        String facilityName3 = "name3";
        String facilityName4 = "name4";
        Facility facility1 = new Facility(new MRSFacility("1", facilityName1, "country", "region", "district", "province"))
                .phoneNumber(phoneNumber1).motechId("facility1");
        Facility facility2 = new Facility(new MRSFacility("2", facilityName2, "country", "region", "district", "province"))
                .phoneNumber(phoneNumber2).additionalPhoneNumber1(additionalPhoneNumber1).motechId("facility2");
        Facility facility3 = new Facility(new MRSFacility("3", facilityName3, "country", "region", "district", "province"))
                .phoneNumber(phoneNumber3).additionalPhoneNumber3(additionalPhoneNumber2).motechId("facility3");
        Facility facility4 = new Facility(new MRSFacility("4", facilityName4, "country", "region", "district", "province"))
                .phoneNumber(phoneNumber4).motechId("facility4");

        AllFacilities allFacilitiesSpy = spy(allFacilities);
        doReturn(Arrays.asList(facility1, facility2, facility3, facility4)).when(allFacilitiesSpy).facilities();

        Map<String,String> expectedFacilities = new HashMap<String, String>();
        expectedFacilities.put("facility1", facilityName1);
        expectedFacilities.put("facility2", facilityName2);
        expectedFacilities.put("facility3", facilityName3);
        expectedFacilities.put("facility4", facilityName4);

        Map<String, String> allPhoneNumbers = allFacilitiesSpy.getAllFacilityNameToMotechFacilityIdMapping();

        assertReflectionEquals(expectedFacilities, allPhoneNumbers);
    }

    @After
    public void tearDown() {
        List<Facility> all = allFacilities.getAll();
        for (Facility facility : all)
            allFacilities.remove(facility);
    }
}
