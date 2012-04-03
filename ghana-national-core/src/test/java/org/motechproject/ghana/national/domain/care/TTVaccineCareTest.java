package org.motechproject.ghana.national.domain.care;

import org.joda.time.LocalDate;
import org.junit.Test;
import org.motechproject.ghana.national.configuration.ScheduleNames;
import org.motechproject.ghana.national.domain.*;
import org.motechproject.ghana.national.vo.Pregnancy;
import org.motechproject.mrs.model.MRSFacility;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.mrs.model.MRSPatient;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.motechproject.util.DateUtil.newDate;
import static org.motechproject.util.DateUtil.today;

public class TTVaccineCareTest {

    @Test
    // TODO #1425: create milestone - once patient care is changes
    public void shouldCreateTTVaccineCareWithCorrectMilestoneGivenAnObservation() {

        LocalDate enrollmentDate = newDate(2012, 3, 3);
        LocalDate ttVaccinationDate = newDate(2012, 1, 1);
        final String facilityId = "fid";

        Patient patient = new Patient(new MRSPatient("pid", "mid", null, new MRSFacility(facilityId)));
        Double ttDose=3.0;
        MRSObservation<String> activePregnancyObs = createPregnacyObservationWithTTDependent(enrollmentDate, ttVaccinationDate,ttDose);
        ActiveCareSchedules mockActiveCareSchedules = mock(ActiveCareSchedules.class);
        when(mockActiveCareSchedules.hasActiveTTSchedule()).thenReturn(true);

        TTVaccineCare vaccineCare = TTVaccineCare.createFrom(patient, enrollmentDate, activePregnancyObs, mockActiveCareSchedules);

        PatientCare expectedPatientCare = new PatientCare(ScheduleNames.TT_VACCINATION, ttVaccinationDate, enrollmentDate, TTVaccineDosage.TT4.name(), new HashMap<String, String>() {{
            put(Patient.FACILITY_META, facilityId);
        }});
        assertThat(this.<PatientCare>getField(vaccineCare, "patientCareBasedOnHistory"), is(expectedPatientCare));
        assertThat(this.<Patient>getField(vaccineCare, "patient"), is(patient));
        assertThat(this.<LocalDate>getField(vaccineCare, "enrollmentDate"), is(enrollmentDate));
        assertThat(this.<Boolean>getField(vaccineCare, "hasActiveTTSchedule"), is(true));
    }

    @Test
    // TODO #1425: create milestone - once patient care is changes
    public void shouldNotCreateScheduleIfActiveScheduleAlreadyExists() {

        LocalDate enrollmentDate = newDate(2012, 3, 3);
        final String facilityId = "fid";
        Patient patient = new Patient(new MRSPatient("pid", "mid", null, new MRSFacility(facilityId)));
        MRSObservation<String> activePregnancyObsWithoutTT = new MRSObservation<String>(enrollmentDate.toDate(), Concept.PREGNANCY.getName(), null);

        ActiveCareSchedules mockActiveCareSchedules = mock(ActiveCareSchedules.class);
        when(mockActiveCareSchedules.hasActiveTTSchedule()).thenReturn(true);

        PatientCare patientCare = TTVaccineCare.createFrom(patient, enrollmentDate, activePregnancyObsWithoutTT, mockActiveCareSchedules).care();

        assertNull(patientCare);
    }

    @Test
    // TODO #1425: create milestone - once patient care is changes
    public void shouldReturnHistoryPatientCareWithNextMilestone_IfActiveScheduleNotExistsAndHistoryIsProvided() {

        LocalDate enrollmentDate = newDate(2012, 3, 3);
        LocalDate ttVaccinationDate = newDate(2012, 1, 1);
        final String facilityId = "fid";

        Patient patient = new Patient(new MRSPatient("pid", "mid", null, new MRSFacility(facilityId)));
        Double ttDose=2.0;
        MRSObservation<String> activePregnancyObs = createPregnacyObservationWithTTDependent(enrollmentDate, ttVaccinationDate,ttDose);
        ActiveCareSchedules noActiveSchedules = mock(ActiveCareSchedules.class);
        when(noActiveSchedules.hasActiveTTSchedule()).thenReturn(false);

        PatientCare patientCare = TTVaccineCare.createFrom(patient, enrollmentDate, activePregnancyObs, noActiveSchedules).care();

        PatientCare expectedPatientCare = new PatientCare(ScheduleNames.TT_VACCINATION, ttVaccinationDate, enrollmentDate, TTVaccineDosage.TT3.name(), new HashMap<String, String>() {{
            put(Patient.FACILITY_META, facilityId);
        }});
        assertThat(patientCare, is(expectedPatientCare));
    }

    @Test
    public void shouldReturnPatientCareWithDefaultStartMilestoneIfNoActiveScheduleExistsAndNoHistoryIsProvided() {

        LocalDate enrollmentDate = newDate(2012, 3, 3);
        final String facilityId = "fid";

        Patient patient = new Patient(new MRSPatient("pid", "mid", null, new MRSFacility(facilityId)));
        MRSObservation<String> activePregnancyObsWithoutTT = new MRSObservation<String>(enrollmentDate.toDate(), Concept.PREGNANCY.getName(), null);
        ActiveCareSchedules noActiveSchedules = mock(ActiveCareSchedules.class);
        when(noActiveSchedules.hasActiveTTSchedule()).thenReturn(false);

        PatientCare patientCare = TTVaccineCare.createFrom(patient, enrollmentDate, activePregnancyObsWithoutTT, noActiveSchedules).care();

        PatientCare expectedPatientCare = new PatientCare(ScheduleNames.TT_VACCINATION, enrollmentDate, enrollmentDate, null, new HashMap<String, String>() {{
            put(Patient.FACILITY_META, facilityId);
        }});
        assertThat(patientCare, is(expectedPatientCare));
    }

    @Test
    public void shouldNotCreatePatientCareIfHistoryProvidedIsTheLastMilestone(){
       LocalDate enrollmentDate = today();
        final String facilityId = "fid";
        Pregnancy pregnancy = Pregnancy.basedOnConceptionDate(enrollmentDate.minusMonths(9));
        LocalDate lastTTVaccinationDate=pregnancy.dateOfConception().plusMonths(6);
        ActiveCareSchedules noActiveSchedules = mock(ActiveCareSchedules.class);
        when(noActiveSchedules.hasActiveTTSchedule()).thenReturn(false);

        Patient patient = new Patient(new MRSPatient("pid", "mid", null, new MRSFacility(facilityId)));
        MRSObservation<String> activePregnancyObs = createPregnacyObservationWithTTDependent(enrollmentDate, lastTTVaccinationDate, 5.0);
        PatientCare patientCare = TTVaccineCare.createFrom(patient, enrollmentDate, activePregnancyObs,noActiveSchedules).care();

        assertNull(patientCare);
    }

    private MRSObservation<String> createPregnacyObservationWithTTDependent(LocalDate enrollmentDate, LocalDate ttVaccinationDate,Double ttDose) {
        MRSObservation<String> activePregnancyObs = new MRSObservation<String>(enrollmentDate.toDate(), Concept.PREGNANCY.getName(), null);
        activePregnancyObs.addDependantObservation(new MRSObservation<Double>(ttVaccinationDate.toDate(), Concept.TT.getName(), ttDose));
        return activePregnancyObs;
    }

    private <T> T getField(Object object, String fieldName) {
        return (T) ReflectionTestUtils.getField(object, fieldName);
    }
}