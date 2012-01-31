package org.motechproject.ghana.national.functional.patient;

import org.junit.runner.RunWith;
import org.motechproject.functional.data.TestPatient;
import org.motechproject.functional.data.TestStaff;
import org.motechproject.functional.pages.patient.CWCEnrollmentPage;
import org.motechproject.functional.pages.patient.PatientEditPage;
import org.motechproject.functional.pages.patient.PatientPage;
import org.motechproject.functional.pages.patient.SearchPatientPage;
import org.motechproject.functional.pages.staff.StaffPage;
import org.motechproject.functional.util.DataGenerator;
import org.motechproject.ghana.national.domain.RegistrationToday;
import org.motechproject.ghana.national.functional.LoggedInUserFunctionalTest;
import org.motechproject.util.DateUtil;
import org.openqa.selenium.By;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
public class CWCTest extends LoggedInUserFunctionalTest {
    private DataGenerator dataGenerator;

    @BeforeMethod
    public void setUp() {
        dataGenerator = new DataGenerator();
    }

    @Test
    public void shouldEnrollForCWCForAPatient() {
        StaffPage staffPage = browser.toStaffCreatePage(homePage);
        staffPage.create(TestStaff.with("First Name" + dataGenerator.randomString(5)));
        String staffId = staffPage.staffId();

        PatientPage patientPage = browser.toCreatePatient(staffPage);
        TestPatient patient = TestPatient.with("First Name" + dataGenerator.randomString(5)).
                registrationMode(TestPatient.PATIENT_REGN_MODE.AUTO_GENERATE_ID).
                patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE).estimatedDateOfBirth(false).dateOfBirth(DateUtil.newDate(2010,11,11));
        patientPage.create(patient);

        SearchPatientPage searchPatientPage = browser.toSearchPatient(homePage);
        searchPatientPage.searchWithName(patient.firstName());

        PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);

        CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);

        cwcEnrollmentPage.withStaffId(staffId).withRegistrationToday(RegistrationToday.IN_PAST.toString()).withSerialNumber("trew654gf")
                .withCountry("Ghana").withRegion("Central Region").withDistrict("Awutu Senya").withSubDistrict("Awutu")
                .withRegistrationDate(DateUtil.newDate(2011, 11, 30)).withAddHistory(false).withFacility("Awutu HC").submit();
        
        assertTrue(cwcEnrollmentPage.getDriver().findElement(By.className("success")).getText().equals("Client registered for CWC successfully."));
    }

}
