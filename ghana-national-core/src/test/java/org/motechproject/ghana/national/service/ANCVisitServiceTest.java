package org.motechproject.ghana.national.service;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.vo.ANCVisit;
import org.motechproject.mrs.model.MRSConcept;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.testing.utils.BaseUnitTest;
import org.motechproject.util.DateUtil;
import org.springframework.test.util.ReflectionTestUtils;
import org.unitils.reflectionassert.ReflectionComparatorMode;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;

public class ANCVisitServiceTest extends BaseUnitTest {
    private ANCVisitService service;
    @Mock
    EncounterService encounterService;
    @Mock
    PatientService patientService;

    @Before
    public void setUp(){
        service = new ANCVisitService();
        initMocks(this);
        ReflectionTestUtils.setField(service, "encounterService", encounterService);
        ReflectionTestUtils.setField(service, "patientService", patientService);
    }
    
    @Test
    public void shouldCreateEncounterForANCVisitWithAllInfo(){
        ANCVisit ancVisit = createTestANCVisit();
        ANCVisitService ancVisitServiceSpy = spy(service);

        Patient mockPatient = mock(Patient.class);
        MRSPatient mockMRSPatient = mock(MRSPatient.class);
        when(patientService.getPatientByMotechId(ancVisit.getMotechId())).thenReturn(mockPatient);
        when(mockPatient.getMrsPatient()).thenReturn(mockMRSPatient);
        when(mockMRSPatient.getId()).thenReturn("34");
        when(mockMRSPatient.getMotechId()).thenReturn(ancVisit.getMotechId());


        Set<MRSObservation> mrsObservations = new HashSet<MRSObservation>();
        mrsObservations.add(new MRSObservation<String>("1",new Date(),"MRSConcept-name","MRSConcept-value"));

        doReturn(mrsObservations).when(ancVisitServiceSpy).createMRSObservations(ancVisit);

        ancVisitServiceSpy.registerANCVisit(ancVisit);

        ArgumentCaptor<MRSPatient> mrsPatientArgumentCaptor = ArgumentCaptor.forClass(MRSPatient.class);
        ArgumentCaptor<String> facilityIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> staffIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> encounterTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Set> setArgumentCaptor = ArgumentCaptor.forClass(Set.class);

        verify(encounterService).persistEncounter(mrsPatientArgumentCaptor.capture(), staffIdArgumentCaptor.capture(),
                            facilityIdArgumentCaptor.capture(), encounterTypeArgumentCaptor.capture(), dateArgumentCaptor.capture(),
                            setArgumentCaptor.capture());

        assertEquals(staffIdArgumentCaptor.getValue(),ancVisit.getStaffId());
        assertEquals(mrsPatientArgumentCaptor.getValue().getMotechId(),ancVisit.getMotechId());
        assertEquals(facilityIdArgumentCaptor.getValue(), ancVisit.getFacilityId());
        assertEquals(encounterTypeArgumentCaptor.getValue(), Constants.ENCOUNTER_ANCVISIT);
        assertReflectionEquals(dateArgumentCaptor.getValue(), DateUtil.today().toDate(), ReflectionComparatorMode.LENIENT_DATES);
        assertEquals(setArgumentCaptor.getValue(),mrsObservations);
    }


    @Test
    public void shouldCreateObservationsWithGivenInfo(){
        ANCVisit ancVisit = createTestANCVisit();
        MRSConcept conceptPositive = new MRSConcept(Constants.CONCEPT_POSITIVE);
        MRSConcept conceptNegative = new MRSConcept(Constants.CONCEPT_NEGATIVE);
        MRSConcept conceptNonReactive = new MRSConcept(Constants.CONCEPT_NON_REACTIVE);
        MRSConcept conceptReactive = new MRSConcept(Constants.CONCEPT_REACTIVE);

        Set<MRSObservation> mrsObservations = service.createMRSObservations(ancVisit);
        Set<MRSObservation> expectedObservations = new HashSet<MRSObservation>();

        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_SERIAL_NUMBER, "4ds65"));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_VISIT_NUMBER, "4"));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_MALE_INVOLVEMENT, false));
        expectedObservations.add(new MRSObservation<Date>(DateUtil.today().toDate(), Constants.CONCEPT_EDD, new Date(2012, 8, 8)));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_COMMENTS, "comments"));
        expectedObservations.add(new MRSObservation<Integer>(DateUtil.today().toDate(), Constants.CONCEPT_ANC_PNC_LOCATION, 34));
        expectedObservations.add(new MRSObservation<Date>(DateUtil.today().toDate(), Constants.CONCEPT_NEXT_ANC_DATE, new Date(2012, 2, 20)));
        expectedObservations.add(new MRSObservation<Integer>(DateUtil.today().toDate(), Constants.CONCEPT_SYSTOLIC_BLOOD_PRESSURE, 10));
        expectedObservations.add(new MRSObservation<Integer>(DateUtil.today().toDate(), Constants.CONCEPT_DIASTOLIC_BLOOD_PRESSURE, 67));
        expectedObservations.add(new MRSObservation<Double>(DateUtil.today().toDate(), Constants.CONCEPT_WEIGHT_KG, 65.67d));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_TT, "4"));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_IPT, "56"));
        expectedObservations.add(new MRSObservation<MRSConcept>(DateUtil.today().toDate(), Constants.CONCEPT_IPT_REACTION, conceptReactive));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_INSECTICIDE_TREATED_NET_USAGE, "itn"));
        expectedObservations.add(new MRSObservation<Double>(DateUtil.today().toDate(), Constants.CONCEPT_FHT, 4.3d));
        expectedObservations.add(new MRSObservation<Integer>(DateUtil.today().toDate(), Constants.CONCEPT_FHR, 4));
        expectedObservations.add(new MRSObservation<MRSConcept>(DateUtil.today().toDate(), Constants.CONCEPT_URINE_GLUCOSE_TEST, conceptNegative));
        expectedObservations.add(new MRSObservation<MRSConcept>(DateUtil.today().toDate(), Constants.CONCEPT_URINE_PROTEIN_TEST, conceptPositive));
        expectedObservations.add(new MRSObservation<Double>(DateUtil.today().toDate(), Constants.CONCEPT_HEMOGLOBIN, 13.8));
        expectedObservations.add(new MRSObservation<MRSConcept>(DateUtil.today().toDate(), Constants.CONCEPT_VDRL, conceptNonReactive));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_VDRL_TREATMENT, true));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_DEWORMER, true));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_PMTCT, true));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_HIV_PRE_TEST_COUNSELING, false));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_HIV_TEST_RESULT, "hiv"));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_HIV_POST_TEST_COUNSELING, true));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_PMTCT_TREATMENT, true));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_HOUSE, "house"));
        expectedObservations.add(new MRSObservation<String>(DateUtil.today().toDate(), Constants.CONCEPT_COMMUNITY, "community"));
        expectedObservations.add(new MRSObservation<Boolean>(DateUtil.today().toDate(), Constants.CONCEPT_REFERRED, true));

        assertReflectionEquals(expectedObservations, mrsObservations, ReflectionComparatorMode.LENIENT_DATES,
                ReflectionComparatorMode.LENIENT_ORDER);
    }

    @Test
    public void shouldFindCurrentWeekFromExpectedDeliveryDate() {

        LocalDate expectedDeliveryDate = new LocalDate(2012, 6, 27);
        LocalDate week12thEndDate = expectedDeliveryDate.minusDays(28 * 7);

        mockCurrentDate(expectedDeliveryDate.minusDays(20 * 7));
        assertThat(service.currentWeekOfPregnancy(expectedDeliveryDate), is(equalTo(20)));

        mockCurrentDate(week12thEndDate);
        assertThat(service.currentWeekOfPregnancy(expectedDeliveryDate), is(equalTo(12)));

        mockCurrentDate(new LocalDate(week12thEndDate.plusDays(1)));
        assertThat(service.currentWeekOfPregnancy(expectedDeliveryDate), is(equalTo(13)));

        mockCurrentDate(new LocalDate(week12thEndDate.plusDays(7)));
        assertThat(service.currentWeekOfPregnancy(expectedDeliveryDate), is(equalTo(13)));

        mockCurrentDate(week12thEndDate.plusDays(8));
        assertThat(service.currentWeekOfPregnancy(expectedDeliveryDate), is(equalTo(14)));
    }


    private ANCVisit createTestANCVisit() {
        ANCVisit ancVisit = new ANCVisit();
        return  (ancVisit.staffId("465").facilityId("232465").motechId("2321465").date(new Date()).serialNumber("4ds65").visitNumber("4").estDeliveryDate(new Date(2012, 8, 8)).
        bpDiastolic(67).bpSystolic(10).weight(65.67d).comments("comments").ttdose("4").iptdose("56").iptReactive("Y").itnUse("itn").fht(4.3d).fhr(4).urineTestGlucosePositive("0").
        urineTestProteinPositive("1").hemoglobin(13.8).vdrlReactive("N").vdrlTreatment("Y").dewormer("Y").pmtct("Y").preTestCounseled("N").
        hivTestResult("hiv").postTestCounseled("Y").pmtctTreament("Y").location("34").house("house").community("community").referred("Y").maleInvolved(false).nextANCDate(new Date(2012, 2, 20)));
    }


}
