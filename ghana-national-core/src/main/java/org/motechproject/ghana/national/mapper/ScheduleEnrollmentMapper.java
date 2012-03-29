package org.motechproject.ghana.national.mapper;

import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.domain.PatientCare;
import org.motechproject.scheduletracking.api.service.EnrollmentRequest;

public class ScheduleEnrollmentMapper {
    public EnrollmentRequest map(Patient patient, PatientCare patientCare) {
        return map(patient, patientCare, null);
    }

    public EnrollmentRequest map(Patient patient, PatientCare patientCare, String startingMilestone) {
        return new EnrollmentRequest(patient.getMRSPatientId(), patientCare.name(),
                patientCare.preferredTime(), patientCare.startingOn(), patientCare.referenceTime(), patientCare.enrollmentDate(), patientCare.enrollmentTime(), startingMilestone, patientCare.metaData());
    }

    public EnrollmentRequest map(String mrsPatientId, String programName) {
        return new EnrollmentRequest(mrsPatientId, programName, null, null, null, null, null, null, null);
    }
}
