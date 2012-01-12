package org.motechproject.ghana.national.repository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.mrs.model.MRSEncounter;
import org.motechproject.openmrs.services.OpenMRSEncounterAdaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class AllEncountersTest {

    @Mock
    OpenMRSEncounterAdaptor mockOpenMRSEncounterAdaptor;

    AllEncounters allEncounters;

    @Before
    public void setUp() {
        allEncounters = new AllEncounters();
        initMocks(this);
        ReflectionTestUtils.setField(allEncounters, "openMRSEncounterAdaptor", mockOpenMRSEncounterAdaptor);
    }

    @Test
    public void shouldSaveAnEncounter() {
        MRSEncounter mrsEncounter = mock(MRSEncounter.class);
        allEncounters.save(mrsEncounter);
        verify(mockOpenMRSEncounterAdaptor).createEncounter(mrsEncounter);
    }

    @Test
    public void shouldFetchTheLatestEncounter() {
        final String encounterType = "Encounter Type";
        final String motechId = "1234567";
        allEncounters.fetchLatest(motechId, encounterType);
        verify(mockOpenMRSEncounterAdaptor).getLatestEncounterByPatientMotechId(motechId, encounterType);
    }
}

