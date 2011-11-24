package org.motechproject.ghana.national.helper;

import ch.lambdaj.group.Group;
import org.apache.commons.lang.StringUtils;
import org.motechproject.ghana.national.domain.Constants;
import org.motechproject.ghana.national.domain.Facility;
import org.motechproject.ghana.national.service.FacilityService;
import org.motechproject.ghana.national.tools.Utility;
import org.motechproject.ghana.national.vo.FacilityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.lambdaj.Lambda.extract;
import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.map;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static ch.lambdaj.Lambda.selectDistinct;
import static ch.lambdaj.group.Groups.by;
import static ch.lambdaj.group.Groups.group;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;

@Component
public class FacilityHelper {

    @Autowired
    FacilityService facilityService;

    public Map<String, Object> locationMap() {
        List<Facility> facilities = facilityService.facilities();

        final HashMap<String, Object> modelMap = new HashMap<String, Object>();
        List<Facility> withValidCountryNames = select(facilities, having(on(Facility.class).country(), is(not(equalTo(StringUtils.EMPTY)))));
        final Group<Facility> byCountryRegion = group(withValidCountryNames, by(on(Facility.class).country()), by(on(Facility.class).region()));
        final Group<Facility> byRegionDistrict = group(withValidCountryNames, by(on(Facility.class).region()), by(on(Facility.class).district()));
        final Group<Facility> byDistrictProvince = group(withValidCountryNames, by(on(Facility.class).district()), by(on(Facility.class).province()));

        modelMap.put(Constants.COUNTRIES, extract(selectDistinct(withValidCountryNames, "country"), on(Facility.class).country()));
        modelMap.put(Constants.REGIONS, Utility.reverseKeyValues(map(byCountryRegion.keySet(), Utility.mapConverter(byCountryRegion))));
        modelMap.put(Constants.DISTRICTS, Utility.reverseKeyValues(map(byRegionDistrict.keySet(), Utility.mapConverter(byRegionDistrict))));
        modelMap.put(Constants.PROVINCES, Utility.reverseKeyValues(map(byDistrictProvince.keySet(), Utility.mapConverter(byDistrictProvince))));
        modelMap.put(Constants.FACILITIES, facilityVOs(facilities));
        return modelMap;
    }

    private List<FacilityVO> facilityVOs(List<Facility> facilities) {
        List<FacilityVO> facilityVOs = new ArrayList<FacilityVO>();
        for (Facility facility : facilities) {
            if (facility.province() != null) {
                facilityVOs.add(new FacilityVO(facility.mrsFacility().getId(), facility.name(), facility.province()));
                continue;
            }
            if (facility.district() != null) {
                facilityVOs.add(new FacilityVO(facility.mrsFacility().getId(), facility.name(), facility.district()));
                continue;
            }
            if (facility.region() != null) {
                facilityVOs.add(new FacilityVO(facility.mrsFacility().getId(), facility.name(), facility.region()));
            }
        }
        return facilityVOs;
    }
}
