package org.motechproject.ghana.national.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.domain.Encounter;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.repository.AllStaffs;
import org.motechproject.mrs.model.*;
import org.motechproject.mrs.services.MRSEncounterAdapter;
import org.motechproject.util.DateUtil;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.motechproject.ghana.national.domain.EncounterType.CWC_VISIT;
import static org.motechproject.ghana.national.domain.EncounterType.PATIENT_REG_VISIT;


public class AllEncountersTest {

    private AllEncounters allEncounters;
    @Mock
    AllStaffs mockAllStaffs;
    @Mock
    MRSEncounterAdapter mrsEncounterAdapter;

    @Before
    public void setUp() {
        initMocks(this);
        allEncounters = new AllEncounters();
        ReflectionTestUtils.setField(allEncounters, "allStaffs", mockAllStaffs);
        ReflectionTestUtils.setField(allEncounters, "mrsEncounterAdapter", mrsEncounterAdapter);
    }

    @Test
    public void shouldPersistEncounter() {
        String staffId = "12345";
        String facilityId = "12312";
        Set<MRSObservation> mrsObservations = new HashSet<MRSObservation>();
        MRSUser mrsUser = new MRSUser().id(staffId).person(new MRSPerson().id("341"));
        when(mockAllStaffs.getUserByEmailIdOrMotechId(staffId)).thenReturn(mrsUser);
        MRSPatient mrsPatient = new MRSPatient("45454");

        allEncounters.persistEncounter(mrsPatient, staffId, facilityId, PATIENT_REG_VISIT.value(), DateUtil.newDate(2011, 9, 1).toDate(), mrsObservations);

        ArgumentCaptor<MRSEncounter> mrsEncounterArgumentCaptor = ArgumentCaptor.forClass(MRSEncounter.class);
        verify(mrsEncounterAdapter).createEncounter(mrsEncounterArgumentCaptor.capture());
        MRSEncounter mrsEncounterSaved = mrsEncounterArgumentCaptor.getValue();
        assertEquals(mrsEncounterSaved.getCreator().getId(), staffId);
        assertEquals(mrsEncounterSaved.getFacility().getId(), facilityId);
        assertEquals(mrsEncounterSaved.getEncounterType(), PATIENT_REG_VISIT.value());
    }

    @Test
    public void shouldPersistEncounterAndReturnMRSEncounter() {
        String staffId = "12345";
        String facilityId = "12312";
        Set<MRSObservation> mrsObservations = new HashSet<MRSObservation>();
        MRSUser mrsUser = new MRSUser().id(staffId).person(new MRSPerson().id("341"));
        Facility facility = new Facility(new MRSFacility(facilityId));
        MRSPatient mrsPatient = new MRSPatient("45454");

        Encounter encounter = new Encounter(mrsPatient, mrsUser, facility, CWC_VISIT, DateUtil.now().toDate(), mrsObservations);
        allEncounters.persistEncounter(encounter);

        ArgumentCaptor<MRSEncounter> mrsEncounterArgumentCaptor = ArgumentCaptor.forClass(MRSEncounter.class);
        verify(mrsEncounterAdapter).createEncounter(mrsEncounterArgumentCaptor.capture());
        MRSEncounter mrsEncounterSaved = mrsEncounterArgumentCaptor.getValue();
        assertEquals(mrsEncounterSaved.getCreator().getId(), staffId);
        assertEquals(mrsEncounterSaved.getFacility().getId(), facilityId);
        assertEquals(mrsEncounterSaved.getEncounterType(), CWC_VISIT.value());
    }

    @Test
    public void shouldFetchLatestEncounter() {
        String encounterType = "encounterType";
        String motechId = "motechId";
        allEncounters.getLatest(motechId, encounterType);
        verify(mrsEncounterAdapter).getLatestEncounterByPatientMotechId(motechId, encounterType);
    }

}
