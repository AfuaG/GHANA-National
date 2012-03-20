package org.motechproject.ghana.national.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.exception.FacilityAlreadyFoundException;
import org.motechproject.ghana.national.exception.FacilityNotFoundException;
import org.motechproject.ghana.national.repository.AllFacilities;
import org.motechproject.ghana.national.repository.IdentifierGenerator;
import org.motechproject.ghana.national.tools.StartsWithMatcher;
import org.motechproject.ghana.national.tools.Utility;
import org.motechproject.mrs.model.MRSFacility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static ch.lambdaj.Lambda.filter;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.core.Is.is;

@Service
public class FacilityService {

    private AllFacilities allFacilities;
    private IdentifierGenerator identifierGenerator;


    @Autowired
    public FacilityService(AllFacilities allFacilities, IdentifierGenerator identifierGenerator) {
        this.allFacilities = allFacilities;
        this.identifierGenerator = identifierGenerator;
    }

    public String create(String name, String country, String region, String district, String province, String phoneNumber, String additionalPhoneNumber1, String additionalPhoneNumber2, String additionalPhoneNumber3) throws FacilityAlreadyFoundException {
        final List<Facility> facilities = facilities();
        final MRSFacility mrsFacility = new MRSFacility(name, country, Utility.emptyToNull(region), Utility.emptyToNull(district), Utility.emptyToNull(province));
        isDuplicate(facilities, mrsFacility, phoneNumber);
        final Facility facility = new Facility(mrsFacility).phoneNumber(phoneNumber).additionalPhoneNumber1(additionalPhoneNumber1).
                additionalPhoneNumber2(additionalPhoneNumber2).additionalPhoneNumber3(additionalPhoneNumber3).motechId(identifierGenerator.newFacilityId());
        save(facility);
        return facility.mrsFacilityId();
    }

    private void save(Facility facility) {
        if (facility.mrsFacilityId() == null)
            allFacilities.add(facility);
        else
            allFacilities.update(facility);
    }

    public List<Facility> facilities() {
        return allFacilities.facilities();
    }

    public List<Facility> searchFacilities(String motechId, String name, String country, String region, String district, String province) {
        List<Facility> filteredFacilities = facilities();
        filteredFacilities = filterFacilities(on(Facility.class).mrsFacility().getCountry(), country, filteredFacilities);
        filteredFacilities = filterFacilities(on(Facility.class).mrsFacility().getRegion(), region, filteredFacilities);
        filteredFacilities = filterFacilities(on(Facility.class).mrsFacility().getCountyDistrict(), district, filteredFacilities);
        filteredFacilities = filterFacilities(on(Facility.class).mrsFacility().getStateProvince(), province, filteredFacilities);
        filteredFacilities = filterFacilities(on(Facility.class).mrsFacility().getName(), name, filteredFacilities);
        filteredFacilities = filterFacilities(on(Facility.class).motechId(), motechId, filteredFacilities);
        return filteredFacilities;
    }

    void isDuplicate(List<Facility> facilities, MRSFacility mrsFacility, String phoneNumber) throws FacilityAlreadyFoundException {
        for (Facility facility : facilities) {
            MRSFacility thatMrsFacility = facility.mrsFacility();
            if ((thatMrsFacility != null
                    && StringUtils.equals(thatMrsFacility.getName(), mrsFacility.getName())
                    && StringUtils.equals(thatMrsFacility.getStateProvince(), mrsFacility.getStateProvince())
                    && StringUtils.equals(thatMrsFacility.getCountyDistrict(), mrsFacility.getCountyDistrict())
                    && StringUtils.equals(thatMrsFacility.getRegion(), mrsFacility.getRegion())
                    && StringUtils.equals(thatMrsFacility.getCountry(), mrsFacility.getCountry()))) {
                throw new FacilityAlreadyFoundException("facility_already_exists");
            }
        }
        isDuplicatePhoneNumber(phoneNumber, facilities);
    }

    void isDuplicatePhoneNumber(String phoneNumber, List<Facility> facilities) throws FacilityAlreadyFoundException {
        List<Facility> facilityWithPhoneNumber = select(facilities, having(on(Facility.class).getPhoneNumber(), is(phoneNumber)));
        if (!CollectionUtils.isEmpty(facilityWithPhoneNumber))
            throw new FacilityAlreadyFoundException("facility_phone_not_unique");
    }

    private List<Facility> filterFacilities(String field, String matcher, List<Facility> filteredFacilities) {
        return (StringUtils.isNotEmpty(matcher)) ? filter(having(field, StartsWithMatcher.ignoreCaseStartsWith(matcher)), filteredFacilities) : filteredFacilities;
    }

    public Facility getFacility(String facilityId) {
        return allFacilities.getFacility(facilityId);
    }

    public void update(Facility facilityToBeUpdated) throws FacilityNotFoundException, FacilityAlreadyFoundException {
        Facility facility = allFacilities.getFacility(facilityToBeUpdated.mrsFacility().getId());
        if (facility == null) {
            throw new FacilityNotFoundException();
        }
        List<Facility> facilities = facilities();
        List<Facility> existingFacility = select(facilities, having(on(Facility.class).getMrsFacilityId(), is(facilityToBeUpdated.mrsFacilityId())));
        if (!existingFacility.isEmpty())
            facilities.remove(existingFacility.get(0));
        isDuplicate(facilities, facilityToBeUpdated.mrsFacility(), facilityToBeUpdated.phoneNumber());
        save(facilityToBeUpdated);
    }

    public Facility getFacilityByMotechId(String motechFacilityId) {
        return allFacilities.getFacilityByMotechId(motechFacilityId);
    }
}