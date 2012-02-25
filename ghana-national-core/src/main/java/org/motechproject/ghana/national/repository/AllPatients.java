package org.motechproject.ghana.national.repository;

import ch.lambdaj.function.convert.Converter;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.mrs.model.MRSPatient;
import org.motechproject.mrs.model.MRSPerson;
import org.motechproject.mrs.services.MRSPatientAdapter;
import org.motechproject.openmrs.services.OpenMRSRelationshipAdapter;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

import static ch.lambdaj.Lambda.convert;

@Repository
public class AllPatients {
    @Autowired
    private MRSPatientAdapter patientAdapter;

    @Autowired
    private OpenMRSRelationshipAdapter openMRSRelationshipAdapter;

    public Patient save(Patient patient) {
        MRSPatient mrsPatient = patientAdapter.savePatient(patient.getMrsPatient());
        return new Patient(mrsPatient, patient.getParentId());
    }

    public Patient patientByOpenmrsId(String patientId) {
        MRSPatient mrsPatient = patientAdapter.getPatient(patientId);
        return (mrsPatient != null) ? new Patient(mrsPatient) : null;
    }

    public Patient patientByMotechId(String id) {
        MRSPatient mrsPatient = getPatientByMotechId(id);
        if (mrsPatient != null) {
            Patient patient = new Patient(mrsPatient);
            Relationship motherRelationship = getMotherRelationship(patient.getMrsPatient().getPerson());
            if (motherRelationship != null) {
                setParentId(patient, motherRelationship);
            }
            return patient;
        }
        return null;
    }

    private void setParentId(Patient patient, Relationship motherRelationship) {
        Person mother = motherRelationship.getPersonA();
        if (mother != null && !mother.getNames().isEmpty()) {
            List<Patient> patients = search(mother.getNames().iterator().next().getFullName(), null);
            if (patients != null && !patients.isEmpty()) {
                patient.parentId(getParentId(mother, patients));
            }
        }
    }

    private String getParentId(Person mother, List<Patient> patients) {
        for (Patient patient : patients) {
            if (patient.getMrsPatient().getPerson().getId().equals(mother.getId().toString())) {
                return patient.getMrsPatient().getMotechId();
            }
        }
        return null;
    }

    private MRSPatient getPatientByMotechId(String id) {
        return patientAdapter.getPatientByMotechId(id);
    }

    public List<Patient> search(String name, String motechId) {
        return convert(patientAdapter.search(name, motechId), new Converter<MRSPatient, Patient>() {
            @Override
            public Patient convert(MRSPatient mrsPatient) {
                return new Patient(mrsPatient);
            }
        });
    }

    public Integer getAgeOfPersonByMotechId(String motechId) {
        return patientAdapter.getAgeOfPatientByMotechId(motechId);
    }

    public String update(Patient patient) {
        return patientAdapter.updatePatient(patient.getMrsPatient());
    }

    public void createMotherChildRelationship(MRSPerson mother, MRSPerson child) {
        openMRSRelationshipAdapter.createMotherChildRelationship(mother.getId(), child.getId());
    }

    public Relationship getMotherRelationship(MRSPerson person) {
        return openMRSRelationshipAdapter.getMotherRelationship(person.getId());
    }

    public Relationship updateMotherChildRelationship(MRSPerson mother, MRSPerson child) {
        return openMRSRelationshipAdapter.updateMotherRelationship(mother.getId(), child.getId());
    }

    public Relationship voidMotherChildRelationship(MRSPerson child) {
        return openMRSRelationshipAdapter.voidRelationship(child.getId());
    }

    public void deceasePatient(Date dateOfDeath, String patientMotechId, String causeOfDeath, String comment) {
        patientAdapter.deceasePatient(patientMotechId, causeOfDeath, dateOfDeath, comment);
    }
}
