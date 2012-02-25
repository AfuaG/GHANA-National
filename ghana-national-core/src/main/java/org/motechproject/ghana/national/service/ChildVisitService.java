package org.motechproject.ghana.national.service;

import org.motechproject.ghana.national.factory.ChildVisitEncounterFactory;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.vo.CWCVisit;
import org.motechproject.mrs.model.MRSEncounter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChildVisitService {
    private AllEncounters allEncounters;

    @Autowired
    public ChildVisitService(AllEncounters allEncounters) {
        this.allEncounters = allEncounters;
    }

    public MRSEncounter save(CWCVisit cwcVisit) {
        return allEncounters.persistEncounter(new ChildVisitEncounterFactory().createEncounter(cwcVisit));
    }
}
