package org.motechproject.ghana.national.domain;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.motechproject.ghana.national.configuration.ScheduleNames;
import org.motechproject.ghana.national.vo.ANCCareHistoryVO;
import org.motechproject.ghana.national.vo.Pregnancy;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;
import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
import org.motechproject.testing.utils.BaseUnitTest;
import org.motechproject.util.DateUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
import static org.motechproject.ghana.national.vo.Pregnancy.basedOnDeliveryDate;

public class PatientTest extends BaseUnitTest {

    DateTime todayAs6June2012;

    @Before
    public void setUp() {
        todayAs6June2012 = new DateTime(2012, 6, 5, 20, 10);
        mockCurrentDate(todayAs6June2012);
    }

    @Test
    public void shouldReturnPatientCareForDeliveryFromExpectedDeliveryDate() {

        Pregnancy pregnancy = basedOnDeliveryDate(todayAs6June2012.plusWeeks(28).plusDays(6).toLocalDate());

        String facilityId = "fid";
        List<PatientCare> patientCares = patient(now().minusYears(26), facilityId).ancCareProgramsToEnrollOnRegistration(pregnancy.dateOfDelivery(), todayAs6June2012.toLocalDate(), noANCHistory(), new ActiveCareSchedules());
        assertPatientCare(patientCares.get(0), patientCare(ANC_DELIVERY, pregnancy.dateOfConception(), todayAs6June2012.toLocalDate(), facilityId));
    }

    @Test
    public void shouldReturnPatientCareForIPTpForIfCurrentPregnancyWeekIsOnOrBeforeWeek19() {

        String facilityId = "fid";
        Patient patient = patient(now().minusYears(26), facilityId);
        Pregnancy pregnancy = basedOnDeliveryDate(todayAs6June2012.plusWeeks(28).plusDays(6).toLocalDate());

        List<PatientCare> patientCares = patient.ancCareProgramsToEnrollOnRegistration(pregnancy.dateOfDelivery(), todayAs6June2012.toLocalDate(), noANCHistory(), new ActiveCareSchedules());

        assertThat(patientCares, hasItem(new PatientCare(ANC_IPT_VACCINE, pregnancy.dateOfConception(), todayAs6June2012.toLocalDate(), facilityMetaData(facilityId))));
    }

    @Test
    public void shouldReturnPatientCareForIPTiForIfCurrentDOBIsOnOrBeforeWeek14() {

        DateTime birthDay = todayAs6June2012.minusWeeks(14).plusDays(1);
        String facilityId = "fid";
        Patient patient = patient(birthDay, facilityId);
        List<PatientCare> patientCares = patient
                .cwcCareProgramToEnrollOnRegistration(todayAs6June2012.toLocalDate(), new ArrayList<CwcCareHistory>());
        assertThat(patientCares, hasItem(new PatientCare(CWC_IPT_VACCINE, birthDay.toLocalDate(), todayAs6June2012.toLocalDate(), facilityMetaData(facilityId))));

        birthDay = todayAs6June2012.minusWeeks(14);
        patientCares = patient(birthDay, facilityId)
                .cwcCareProgramToEnrollOnRegistration(todayAs6June2012.toLocalDate(), new ArrayList<CwcCareHistory>());
        assertThat(patientCares, not(hasItem(new PatientCare(CWC_IPT_VACCINE, birthDay.toLocalDate(), todayAs6June2012.toLocalDate(), facilityMetaData(facilityId)))));
    }

    @Test
    public void shouldNotReturnPatientCareForIPTpForIfCurrentPregnancyWeekIsAfterWeek13() {

        Pregnancy pregnancy = basedOnDeliveryDate(todayAs6June2012.plusWeeks(12).plusDays(6).toLocalDate());
        String facilityId = "fid";
        List<PatientCare> patientCares = patient(now().minusYears(26), facilityId).ancCareProgramsToEnrollOnRegistration(pregnancy.dateOfDelivery(), todayAs6June2012.toLocalDate(), noANCHistory(), new ActiveCareSchedules());

        assertThat(patientCares, not(hasItem(new PatientCare(ScheduleNames.ANC_IPT_VACCINE, todayAs6June2012.toLocalDate(), todayAs6June2012.toLocalDate(), facilityMetaData(facilityId)))));
    }

    @Test
    public void shouldReturnAllCWCCareProgramsApplicableDuringRegistration() {
        DateTime dateOfBirthWithTime = todayAs6June2012.minusMonths(1);
        String facilityId = "fid";
        Patient patient = patient(dateOfBirthWithTime, facilityId);
        List<PatientCare> patientCares = patient.cwcCareProgramToEnrollOnRegistration(todayAs6June2012.toLocalDate(), new ArrayList<CwcCareHistory>());
        HashMap<String, String> metaData = facilityMetaData(facilityId);

        LocalDate expectedReferenceDate = dateOfBirthWithTime.toLocalDate();
        assertPatientCares(patientCares, asList(new PatientCare(CWC_BCG, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData),
                new PatientCare(CWC_YELLOW_FEVER, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData),
                new PatientCare(CWC_PENTA, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData),
                new PatientCare(CWC_MEASLES_VACCINE, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData),
                new PatientCare(CWC_IPT_VACCINE, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData),
                new PatientCare(CWC_OPV_0, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData),
                new PatientCare(CWC_OPV_OTHERS, expectedReferenceDate, todayAs6June2012.toLocalDate(), metaData)));
    }

    @Test
    public void shouldNotReturnMeaslesPatientCareForCWCRegistration_IfAgeIsMoreThanAYear() {
        DateTime dateOfBirth5YearBack = todayAs6June2012.minusYears(5);
        String facilityId = "fid";
        Patient patient = patient(dateOfBirth5YearBack, facilityId);
        LocalDate expectedReferenceDate = dateOfBirth5YearBack.toLocalDate();
        assertThat(patient.cwcCareProgramToEnrollOnRegistration(todayAs6June2012.toLocalDate(),
                new ArrayList<CwcCareHistory>()), not(hasItem(new PatientCare(CWC_MEASLES_VACCINE, expectedReferenceDate, todayAs6June2012.toLocalDate(), facilityMetaData(facilityId)))));
    }

    @Test
    public void shouldNotReturnPatientCareIfHistoryIsRecordedForBcgYfMeasles(){
        DateTime birthdate = now();
        String facilityId = "fid";
        Patient patient = patient(birthdate, facilityId);
        LocalDate expectedReferenceDate = birthdate.toLocalDate();
        LocalDate enrollmentDate = expectedReferenceDate.plusWeeks(1);

        List<CwcCareHistory> cwcCareHistories = Arrays.asList(CwcCareHistory.BCG,CwcCareHistory.MEASLES,CwcCareHistory.YF);
        List<PatientCare> patientCares = patient.cwcCareProgramToEnrollOnRegistration(enrollmentDate, cwcCareHistories);
        HashMap<String, String> metaData = facilityMetaData(facilityId);
        assertThat(patientCares,not(hasItem(new PatientCare(CWC_BCG, expectedReferenceDate,enrollmentDate, metaData))));
        assertThat(patientCares,not(hasItem(new PatientCare(CWC_YELLOW_FEVER, expectedReferenceDate,enrollmentDate, metaData))));
        assertThat(patientCares,not(hasItem(new PatientCare(CWC_MEASLES_VACCINE, expectedReferenceDate,enrollmentDate, metaData))));

    }

    @Test
    public void shouldReturnPatientCaresForPNCChildProgram() {
        DateTime dateOfBirth = todayAs6June2012.minusDays(5);
        String facilityId = "fid";
        Patient patient = patient(dateOfBirth, facilityId);
        List<PatientCare> cares = patient.pncBabyProgramsToEnrollOnRegistration();
        HashMap<String, String> metaData = facilityMetaData(facilityId);
        assertPatientCares(cares, asList(
                new PatientCare(PNC_CHILD_1, dateOfBirth, dateOfBirth, metaData),
                new PatientCare(PNC_CHILD_2, dateOfBirth, dateOfBirth, metaData),
                new PatientCare(PNC_CHILD_3, dateOfBirth, dateOfBirth, metaData)));
    }

    private HashMap<String, String> facilityMetaData(final String facilityId) {
        return new HashMap<String, String>(){{
            put(Patient.FACILITY_META, facilityId);
        }};
    }

    @Test
    public void shouldReturnPatientCaresForPNCMotherProgram() {
        DateTime deliveryDate = todayAs6June2012.minusDays(10);
        String facilityId = "fid";
        Patient patient = patient(todayAs6June2012, facilityId);
        List<PatientCare> cares = patient.pncMotherProgramsToEnrollOnRegistration(deliveryDate);
        HashMap<String, String> metaData = facilityMetaData(facilityId);
        assertPatientCares(cares, asList(
                new PatientCare(PNC_MOTHER_1, deliveryDate, deliveryDate, metaData),
                new PatientCare(PNC_MOTHER_2, deliveryDate, deliveryDate, metaData),
                new PatientCare(PNC_MOTHER_3, deliveryDate, deliveryDate, metaData)));
    }

    @Test
    public void shouldReturnPatientCareForTTVaccineIfNoActiveScheduleAndNoHistoryIsPresent() {
        LocalDate registrationDate = todayAs6June2012.toLocalDate();
        String facilityId = "fid";
        Patient patient = patient(now().minusYears(30), facilityId);

        EnrollmentRecord ttEnrollmentRecord = mock(EnrollmentRecord.class);
        List<PatientCare> patientCares = patient.ancCareProgramsToEnrollOnRegistration(DateUtil.newDate(2000, 1, 1), registrationDate, null, new ActiveCareSchedules().setActiveCareSchedule(TT_VACCINATION, ttEnrollmentRecord));
        assertThat(patientCares, not(hasItem(new PatientCare(TT_VACCINATION, registrationDate, registrationDate, facilityMetaData(facilityId)))));

        patientCares = patient.ancCareProgramsToEnrollOnRegistration(DateUtil.newDate(2000, 1, 1), registrationDate, null, new ActiveCareSchedules());
        assertThat(patientCares, hasItem(new PatientCare(TT_VACCINATION, registrationDate, registrationDate, facilityMetaData(facilityId))));
    }

    private void assertPatientCare(PatientCare patientCare, PatientCare expected) {
        assertThat(patientCare.name(), is(expected.name()));
        assertThat(patientCare.startingOn(), is(expected.startingOn()));
        assertThat(patientCare.referenceTime(), is(expected.referenceTime()));
        assertThat(patientCare.enrollmentDate(), is(expected.enrollmentDate()));
        assertThat(patientCare.enrollmentTime(), is(expected.enrollmentTime()));
        assertThat(patientCare.preferredTime(), is(expected.preferredTime()));
    }

    private void assertPatientCares(List<PatientCare> actualList, List<PatientCare> expectedList) {
        assertThat(actualList.size(), is(expectedList.size()));
        for (PatientCare expectedCare : expectedList)
            assertThat("Missing " + expectedCare.name(), actualList, hasItem(expectedCare));
    }

    private PatientCare patientCare(String name, LocalDate reference, LocalDate enrollmentDate, String facilityId) {
        return new PatientCare(name, reference, enrollmentDate, facilityMetaData(facilityId));
    }

    private Patient patient(DateTime birthDay, String facilityId) {
        return new Patient(new MRSPatient(null, new MRSPerson().dateOfBirth(birthDay.toDate()), new MRSFacility(facilityId, "fname", "fcountry", "fregion", "fcountry", "state")));
    }

    private ANCCareHistoryVO noANCHistory() {
        return new ANCCareHistoryVO(false, new ArrayList<ANCCareHistory>(), null, null, null, null);
    }

    public static Patient createPatient(String patientId, String motechId, LocalDate dob, String facilityId) {
        return new Patient(new MRSPatient(patientId, motechId, new MRSPerson().dateOfBirth(dob.toDate()), new MRSFacility(facilityId)));
    }
}
