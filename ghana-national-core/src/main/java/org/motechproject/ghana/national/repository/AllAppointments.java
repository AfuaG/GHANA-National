package org.motechproject.ghana.national.repository;

import org.joda.time.DateTime;
import org.motechproject.appointments.api.contract.AppointmentService;
import org.motechproject.appointments.api.contract.CreateVisitRequest;
import org.motechproject.appointments.api.contract.ReminderConfiguration;
import org.motechproject.ghana.national.domain.EncounterType;
import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public class AllAppointments {

    @Autowired
    AppointmentService appointmentService;
    static final int ONE_WEEK_BEFORE = 7;   //to be in sync with motech-appointments api
    static final int ONE_WEEK_LATER = -7;   //to be in sync with motech-appointments api
    static final int TWO_WEEK_LATER = -14;   //to be in sync with motech-appointments api
//    static final int THREE_WEEKS_LATER = -21;

    public void remove(Patient patient) {
        appointmentService.removeCalendar(patient.getMotechId());
    }

    public void updateANCVisitSchedule(Patient patient, Date visitedDate, DateTime nextVisitDate) {
        appointmentService.visited(patient.getMotechId(), EncounterType.ANC_VISIT.value(), DateUtil.newDateTime(visitedDate));
        CreateVisitRequest createVisitRequest = new CreateVisitRequest().setAppointmentDueDate(nextVisitDate).setVisitName(EncounterType.ANC_VISIT.value());

        createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration().setRemindFrom(ONE_WEEK_BEFORE));
        createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration());
        createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration().setRemindFrom(ONE_WEEK_LATER));
        createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration().setRemindFrom(TWO_WEEK_LATER));
        appointmentService.addVisit(patient.getMotechId(), createVisitRequest);
    }
}
