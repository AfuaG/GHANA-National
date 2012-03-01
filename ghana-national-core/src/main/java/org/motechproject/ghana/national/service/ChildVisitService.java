package org.motechproject.ghana.national.service;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.motechproject.ghana.national.configuration.ScheduleNames;
import org.motechproject.ghana.national.domain.Concept;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.factory.ChildVisitEncounterFactory;
import org.motechproject.ghana.national.mapper.ScheduleEnrollmentMapper;
import org.motechproject.ghana.national.repository.AllEncounters;
import org.motechproject.ghana.national.repository.AllSchedules;
import org.motechproject.ghana.national.vo.CWCVisit;
import org.motechproject.mrs.model.MRSEncounter;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChildVisitService extends VisitService {
    private AllEncounters allEncounters;
    private AllSchedules allSchedules;

    @Autowired
    public ChildVisitService(AllEncounters allEncounters, AllSchedules allSchedules) {
        super(allSchedules);
        this.allEncounters = allEncounters;
        this.allSchedules = allSchedules;
    }

    public MRSEncounter save(CWCVisit cwcVisit) {
        updatePentaSchedule(cwcVisit);
        updateYellowFeverSchedule(cwcVisit);
        return allEncounters.persistEncounter(new ChildVisitEncounterFactory().createEncounter(cwcVisit));
    }

    void updateYellowFeverSchedule(CWCVisit cwcVisit) {
        if (cwcVisit.getImmunizations().contains(Concept.YF.name())) {
            Patient patient = cwcVisit.getPatient();
            LocalDate visitDate = DateUtil.newDate(cwcVisit.getDate());
            allSchedules.fulfilCurrentMilestone(patient.getMRSPatientId(), ScheduleNames.YELLOW_FEVER, visitDate);
        }
    }

    void updatePentaSchedule(CWCVisit cwcVisit) {
        if (!StringUtils.isEmpty(cwcVisit.getPentadose())) {
            Patient patient = cwcVisit.getPatient();
            LocalDate visitDate = DateUtil.newDate(cwcVisit.getDate());

            if (null == enrollment(patient.getMRSPatientId(), ScheduleNames.PENTA)) {
                allSchedules.enroll(new ScheduleEnrollmentMapper().map(patient, patient.pentaPatientCare(), visitDate, milestoneName(cwcVisit)));
            }
            allSchedules.fulfilCurrentMilestone(patient.getMRSPatientId(), ScheduleNames.PENTA, visitDate);
        }
    }

    private String milestoneName(CWCVisit cwcVisit) {
        return PentaDose.byValue(Integer.parseInt(cwcVisit.getPentadose())).milestoneName();
    }
}
