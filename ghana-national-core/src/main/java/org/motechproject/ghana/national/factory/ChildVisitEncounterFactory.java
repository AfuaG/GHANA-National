package org.motechproject.ghana.national.factory;

import org.motechproject.ghana.national.domain.Concept;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Encounter;
import org.motechproject.ghana.national.service.request.PNCBabyRequest;
import org.motechproject.ghana.national.domain.CWCVisit;
import org.motechproject.mrs.model.MRSConcept;
import org.motechproject.mrs.model.MRSObservation;
import org.motechproject.util.DateUtil;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.motechproject.ghana.national.domain.Concept.*;
import static org.motechproject.ghana.national.domain.EncounterType.CWC_VISIT;
import static org.motechproject.ghana.national.domain.EncounterType.PNC_CHILD_VISIT;

public class ChildVisitEncounterFactory extends BaseObservationFactory {

    public Encounter createEncounter(CWCVisit cwcVisit) {
        return new Encounter(cwcVisit.getPatient().getMrsPatient(), cwcVisit.getStaff(),
                cwcVisit.getFacility(), CWC_VISIT, cwcVisit.getDate(), createMRSObservations(cwcVisit));
    }

    Set<MRSObservation> createMRSObservations(CWCVisit cwcVisit) {
        HashSet<MRSObservation> mrsObservations = new HashSet<MRSObservation>();
        Date registrationDate = cwcVisit.getDate() == null ? DateUtil.today().toDate() : cwcVisit.getDate();

        setObservation(mrsObservations, registrationDate, SERIAL_NUMBER.getName(), cwcVisit.getSerialNumber());
        setObservation(mrsObservations, registrationDate, MALE_INVOLVEMENT.getName(), cwcVisit.getMaleInvolved());
        setObservation(mrsObservations, registrationDate, WEIGHT_KG.getName(), cwcVisit.getWeight());
        setObservation(mrsObservations, registrationDate, HEIGHT.getName(), cwcVisit.getHeight());
        setObservation(mrsObservations, registrationDate, MUAC.getName(), cwcVisit.getMuac());
        if (!Constants.NOT_APPLICABLE.equals(cwcVisit.getCwcLocation()))
            setObservation(mrsObservations, registrationDate, CWC_LOCATION.getName(), toInteger(cwcVisit.getCwcLocation()));
        setObservation(mrsObservations, registrationDate, HOUSE.getName(), cwcVisit.getHouse());
        setObservation(mrsObservations, registrationDate, COMMUNITY.getName(), cwcVisit.getCommunity());
        setObservation(mrsObservations, registrationDate, COMMENTS.getName(), cwcVisit.getComments());
        setObservation(mrsObservations, registrationDate, PENTA.getName(), toInteger(cwcVisit.getPentadose()));
        setObservation(mrsObservations, registrationDate, ROTAVIRUS.getName(), toInteger(cwcVisit.getRotavirusdose()));
        setObservation(mrsObservations, registrationDate, OPV.getName(), toInteger(cwcVisit.getOpvdose()));
        setObservation(mrsObservations, registrationDate, IPTI.getName(), toInteger(cwcVisit.getIptidose()));
        setObservation(mrsObservations, registrationDate, PNEUMOCOCCAL.getName(), toInteger(cwcVisit.getPneumococcaldose()));
        addImmunizationsOrdered(cwcVisit, mrsObservations, registrationDate, BCG, null);
        addImmunizationsOrdered(cwcVisit, mrsObservations, registrationDate, MEASLES, cwcVisit.getMeaslesDose());
        addImmunizationsOrdered(cwcVisit, mrsObservations, registrationDate, VITA, cwcVisit.getVitaminadose());
        addImmunizationsOrdered(cwcVisit, mrsObservations, registrationDate, YF, null);
        addImmunizationsOrdered(cwcVisit, mrsObservations, registrationDate, DEWORMER, null);
        return mrsObservations;
    }

    private void addImmunizationsOrdered(CWCVisit cwcVisit, HashSet<MRSObservation> mrsObservations, Date registrationDate, Concept concept, String dose) {
        if (cwcVisit.getImmunizations().contains(concept.name())) {
            if(dose != null) {
                concept = Concept.getConceptByName(concept.getName() + " " + dose.toUpperCase());
            }
            setObservation(mrsObservations, registrationDate, IMMUNIZATIONS_ORDERED.getName(), new MRSConcept(concept.getName()));
        }
    }

    public Encounter createEncounter(PNCBabyRequest pncBabyRequest) {
        return new Encounter(pncBabyRequest.getPatient().getMrsPatient(), pncBabyRequest.getStaff(),
                pncBabyRequest.getFacility(), PNC_CHILD_VISIT, pncBabyRequest.getDate().toDate(), createMRSObservations(pncBabyRequest));
    }

    private Set<MRSObservation> createMRSObservations(PNCBabyRequest pncBabyRequest) {
        HashSet<MRSObservation> mrsObservations = new HashSet<MRSObservation>();
        Date pncVisitDate = pncBabyRequest.getDate().toDate();

        setObservation(mrsObservations, pncVisitDate, VISIT_NUMBER.getName(), pncBabyRequest.getVisit().visitNumber());
        setObservation(mrsObservations, pncVisitDate, TEMPERATURE.getName(), pncBabyRequest.getTemperature());
        setObservation(mrsObservations, pncVisitDate, MALE_INVOLVEMENT.getName(), pncBabyRequest.getMaleInvolved());
        setObservation(mrsObservations, pncVisitDate, RESPIRATION.getName(), pncBabyRequest.getRespiration());
        setObservation(mrsObservations, pncVisitDate, CORD_CONDITION.getName(), pncBabyRequest.getCordConditionNormal());
        setObservation(mrsObservations, pncVisitDate, BABY_CONDITION.getName(), pncBabyRequest.getBabyConditionGood());
        setObservation(mrsObservations, pncVisitDate, WEIGHT_KG.getName(), pncBabyRequest.getWeight());
        setObservation(mrsObservations, pncVisitDate, BCG.getName(), toString(pncBabyRequest.getBcg()));
        setObservation(mrsObservations, pncVisitDate, OPV.getName(), pncBabyRequest.getOpv0());
        if (!pncBabyRequest.getLocation().equals(Constants.NOT_APPLICABLE))
            setObservation(mrsObservations, pncVisitDate, ANC_PNC_LOCATION.getName(), toInteger(pncBabyRequest.getLocation()));
        setObservation(mrsObservations, pncVisitDate, HOUSE.getName(), pncBabyRequest.getHouse());
        setObservation(mrsObservations, pncVisitDate, COMMUNITY.getName(), pncBabyRequest.getCommunity());
        setObservation(mrsObservations, pncVisitDate, REFERRED.getName(), pncBabyRequest.getReferred());
        setObservation(mrsObservations, pncVisitDate, COMMENTS.getName(), pncBabyRequest.getComments());
        return mrsObservations;
    }
}
