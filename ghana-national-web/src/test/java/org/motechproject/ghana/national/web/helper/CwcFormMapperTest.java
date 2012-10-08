package org.motechproject.ghana.national.web.helper;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.CwcCareHistory;
import org.motechproject.ghana.national.domain.RegistrationToday;
import org.motechproject.ghana.national.web.CWCController;
import org.motechproject.ghana.national.web.form.CWCEnrollmentForm;
import org.motechproject.mrs.model.*;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.motechproject.ghana.national.domain.Concept.*;

public class CwcFormMapperTest {

    @Test
    public void shouldSetViewAttributes() {
        final CwcFormMapper cwcFormMapper = new CwcFormMapper();
        final Map<String, Object> actual = cwcFormMapper.setViewAttributes();

        assertEquals(new LinkedHashMap<CwcCareHistory, String>() {{
            put(CwcCareHistory.BCG, CwcCareHistory.BCG.getDescription());
            put(CwcCareHistory.IPTI, CwcCareHistory.IPTI.getDescription());
            put(CwcCareHistory.MEASLES, CwcCareHistory.MEASLES.getDescription());
            put(CwcCareHistory.OPV, CwcCareHistory.OPV.getDescription());
            put(CwcCareHistory.PENTA, CwcCareHistory.PENTA.getDescription());
            put(CwcCareHistory.VITA_A, CwcCareHistory.VITA_A.getDescription());
            put(CwcCareHistory.YF, CwcCareHistory.YF.getDescription());
            put(CwcCareHistory.ROTAVIRUS, CwcCareHistory.ROTAVIRUS.getDescription());
            put(CwcCareHistory.PNEUMOCOCCAL, CwcCareHistory.PNEUMOCOCCAL.getDescription());
        }}, actual.get(Constants.CARE_HISTORIES));

        assertEquals(new LinkedHashMap<RegistrationToday, String>() {{
            put(RegistrationToday.TODAY, RegistrationToday.TODAY.getDescription());
            put(RegistrationToday.IN_PAST, RegistrationToday.IN_PAST.getDescription());
            put(RegistrationToday.IN_PAST_IN_OTHER_FACILITY, RegistrationToday.IN_PAST_IN_OTHER_FACILITY.getDescription());
        }}, actual.get(CWCController.REGISTRATION_OPTIONS));

        assertEquals(new LinkedHashMap<Integer, String>() {{
            put(1, Constants.IPTI_1);
            put(2, Constants.IPTI_2);
            put(3, Constants.IPTI_3);
        }}, actual.get(Constants.LAST_IPTI));

        assertEquals(new LinkedHashMap<Integer, String>() {{
            put(0, Constants.OPV_0);
            put(1, Constants.OPV_1);
            put(2, Constants.OPV_2);
            put(3, Constants.OPV_3);
        }}, actual.get(Constants.LAST_OPV));

        assertEquals(new LinkedHashMap<Integer, String>() {{
            put(1, Constants.PENTA_1);
            put(2, Constants.PENTA_2);
            put(3, Constants.PENTA_3);

        }}, actual.get(Constants.LAST_PENTA));

        assertEquals(new LinkedHashMap<Integer, String>() {{
            put(1, Constants.ROTAVIRUS_1);
            put(2, Constants.ROTAVIRUS_2);

        }}, actual.get(Constants.LAST_ROTAVIRUS));

        assertEquals(new LinkedHashMap<String, String>() {{
            put(Constants.VITAMIN_A_BLUE, StringUtils.capitalize(Constants.VITAMIN_A_BLUE));
            put(Constants.VITAMIN_A_RED, StringUtils.capitalize(Constants.VITAMIN_A_RED));
        }}, actual.get(Constants.LAST_VITA));
        assertEquals(new LinkedHashMap<Integer, String>() {{
            put(1, Constants.MEASLES_1);
            put(2, Constants.MEASLES_2);
        }}, actual.get(Constants.LAST_MEASLES));

        assertEquals(new LinkedHashMap<Integer, String>() {{
            put(1, Constants.PNEUMOCOCCAL_1);
            put(2, Constants.PNEUMOCOCCAL_2);
            put(3, Constants.PNEUMOCOCCAL_3);

        }}, actual.get(Constants.LAST_PNEUMOCOCCAL));
    }

    @Test
    public void shouldConvertEncounterToFormView() {
        final CwcFormMapper cwcFormMapper = new CwcFormMapper();

        String providerId = "121";
        String creatorId = "3232";
        String facilityId = "32";
        Date registrationDate = new Date();
        String patientId = "2343";
        final HashSet<MRSObservation> observations = new HashSet<MRSObservation>();
        MRSConcept yfValue = new MRSConcept(YF.getName());
        MRSConcept bcgValue = new MRSConcept(BCG.getName());

        Date measlesDate = new Date();
        Date yfDate = new Date();
        Date bcgDate = new Date();
        Date vitaDate = new Date();
        Date iptiDate = new Date();
        Date opvDate = new Date();
        Date pentaDate = new Date();
        Date rotavirusDate = new Date();
        Date pneumococcalDate = new Date();
        Double iptiValue = 0.0;
        String serialNum = "serial number";
        Double pentaValue = 2.0;
        Double rotavirusDoseValue = 2.0;
        Double pneumococcalDoseValue = 2.0;
        Double opvValue = 2.0;
        Double measlesValue = 1.;
        String vitaValue = "blue";
        String name = "name";
        String country = "country";
        String region = "region";
        String county = "county";
        String province = "province";
        observations.add(new MRSObservation<MRSConcept>(yfDate, IMMUNIZATIONS_ORDERED.getName(), yfValue));
        observations.add(new MRSObservation<MRSConcept>(bcgDate, IMMUNIZATIONS_ORDERED.getName(), bcgValue));
        observations.add(new MRSObservation<Double>(iptiDate, IPTI.getName(), iptiValue));
        observations.add(new MRSObservation<Double>(measlesDate, MEASLES.getName(), measlesValue));
        observations.add(new MRSObservation<String>(vitaDate, VITA.getName(), null));
        observations.add(new MRSObservation<Double>(pentaDate, PENTA.getName(), pentaValue));
        observations.add(new MRSObservation<Double>(rotavirusDate, ROTAVIRUS.getName(), rotavirusDoseValue));
        observations.add(new MRSObservation<Double>(pneumococcalDate, PNEUMOCOCCAL.getName(), pneumococcalDoseValue));
        observations.add(new MRSObservation<Double>(opvDate, OPV.getName(), opvValue));
        observations.add(new MRSObservation<String>(registrationDate, SERIAL_NUMBER.getName(), serialNum));
        MRSFacility facility = new MRSFacility(facilityId, name, country, region, county, province);
        MRSEncounter mrsEncounter = new MRSEncounter("1", new MRSPerson().id(providerId),
                new MRSUser().systemId(creatorId), facility, registrationDate, new MRSPatient(patientId, null, null), observations, "type");

        final CWCEnrollmentForm cwcEnrollmentForm = cwcFormMapper.mapEncounterToView(mrsEncounter);

        assertThat(cwcEnrollmentForm.getPatientMotechId(), is(equalTo(patientId)));
        assertThat(cwcEnrollmentForm.getStaffId(), is(equalTo(creatorId)));
        assertThat(cwcEnrollmentForm.getFacilityForm().getFacilityId(), is(equalTo(facility.getId())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getCountry(), is(equalTo(facility.getCountry())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getCountyDistrict(), is(equalTo(facility.getCountyDistrict())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getName(), is(equalTo(facility.getName())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getStateProvince(), is(equalTo(facility.getStateProvince())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getRegion(), is(equalTo(facility.getRegion())));

        assertThat(cwcEnrollmentForm.getMeaslesDate(), is(equalTo(measlesDate)));
        assertThat(cwcEnrollmentForm.getBcgDate(), is(equalTo(bcgDate)));
        assertThat(cwcEnrollmentForm.getYfDate(), is(equalTo(yfDate)));
        assertThat(cwcEnrollmentForm.getLastVitaminA(), is(equalTo(vitaValue)));
        assertThat(cwcEnrollmentForm.getLastMeasles(), is(equalTo(measlesValue.intValue())));
        assertThat(cwcEnrollmentForm.getVitADate(), is(equalTo(vitaDate)));
        assertThat(cwcEnrollmentForm.getLastIPTiDate(), is(equalTo(iptiDate)));
        assertThat(cwcEnrollmentForm.getLastIPTi(), is(equalTo(iptiValue.intValue())));
        assertThat(cwcEnrollmentForm.getLastPentaDate(), is(equalTo(pentaDate)));
        assertThat(cwcEnrollmentForm.getLastPenta(), is(equalTo(pentaValue.intValue())));
        assertThat(cwcEnrollmentForm.getLastRotavirusDate(), is(equalTo(rotavirusDate)));
        assertThat(cwcEnrollmentForm.getLastRotavirus(), is(equalTo(rotavirusDoseValue.intValue())));
        assertThat(cwcEnrollmentForm.getLastPneumococcal(), is(equalTo(pneumococcalDoseValue.intValue())));
        assertThat(cwcEnrollmentForm.getLastPneumococcalDate(), is(equalTo(pneumococcalDate)));
        assertThat(cwcEnrollmentForm.getLastOPVDate(), is(equalTo(opvDate)));
        assertThat(cwcEnrollmentForm.getLastOPV(), is(equalTo(opvValue.intValue())));
        assertThat(cwcEnrollmentForm.getSerialNumber(), is(equalTo(serialNum)));
        assertThat(cwcEnrollmentForm.getAddHistory(), is(equalTo(true)));
    }

    @Test
    public void shouldConvertEncounterToFormViewAndSetAddHistoryToFalseIfNoObservationsAreFound() {
        final CwcFormMapper cwcFormMapper = new CwcFormMapper();

        String providerId = "121";
        String creatorId = "3232";
        String facilityId = "32";
        Date registrationDate = new Date();
        String patientId = "2343";
        final HashSet<MRSObservation> observations = new HashSet<MRSObservation>();

        String serialNum = "serial number";
        String name = "name";
        String country = "country";
        String region = "region";
        String county = "county";
        String province = "province";
        observations.add(new MRSObservation<String>(registrationDate, SERIAL_NUMBER.getName(), serialNum));
        MRSFacility facility = new MRSFacility(facilityId, name, country, region, county, province);
        MRSEncounter mrsEncounter = new MRSEncounter("1", new MRSPerson().id(providerId),
                new MRSUser().systemId(creatorId), facility, registrationDate, new MRSPatient(patientId, null, null), observations, "type");

        final CWCEnrollmentForm cwcEnrollmentForm = cwcFormMapper.mapEncounterToView(mrsEncounter);

        assertThat(cwcEnrollmentForm.getPatientMotechId(), is(equalTo(patientId)));
        assertThat(cwcEnrollmentForm.getStaffId(), is(equalTo(creatorId)));
        assertThat(cwcEnrollmentForm.getFacilityForm().getFacilityId(), is(equalTo(facility.getId())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getCountry(), is(equalTo(facility.getCountry())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getCountyDistrict(), is(equalTo(facility.getCountyDistrict())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getName(), is(equalTo(facility.getName())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getStateProvince(), is(equalTo(facility.getStateProvince())));
        assertThat(cwcEnrollmentForm.getFacilityForm().getRegion(), is(equalTo(facility.getRegion())));

        assertThat(cwcEnrollmentForm.getSerialNumber(), is(equalTo(serialNum)));
        assertThat(cwcEnrollmentForm.getAddHistory(), is(equalTo(false)));
    }

}
