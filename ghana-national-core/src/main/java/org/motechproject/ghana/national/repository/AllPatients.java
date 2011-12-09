package org.motechproject.ghana.national.repository;

import org.ektorp.CouchDbConnector;
import org.motechproject.dao.MotechAuditableRepository;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.services.MRSPatientAdaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class AllPatients extends MotechAuditableRepository<Patient> {
    private MRSPatientAdaptor patientAdaptor;

    @Autowired
    protected AllPatients(@Qualifier("couchDbConnector") CouchDbConnector db, MRSPatientAdaptor patientAdaptor) {
        super(Patient.class, db);
        this.patientAdaptor = patientAdaptor;
    }

    @Override
    public void add(Patient patient) {
        final MRSPatient savedPatient = patientAdaptor.savePatient(patient.mrsPatient());
        super.add(patient.mrsPatientId(savedPatient.getId()));
    }

    public Patient patientById(String id) {
        MRSPatient mrsPatient = patientAdaptor.getPatientByMotechId(id);
        return (mrsPatient != null) ? new Patient(mrsPatient) : null;
    }
}
