package org.ghana.national.tools;

import ch.lambdaj.function.convert.Converter;
import ch.lambdaj.group.Group;
import org.junit.Test;
import org.motechproject.mrs.services.Facility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.group.Groups.by;
import static ch.lambdaj.group.Groups.group;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class UtilityTest {

    @Test
    public void testMapConverter() {
        final String country = "country";
        final String region = "region";
        ArrayList<Facility> facilities = new ArrayList<Facility>() {
            {
                add(new Facility("facility", country, region, "district", "state"));
            }
        };
        Group<Facility> facilityGroup = group(facilities, by(on(Facility.class).getCountry()), by(on(Facility.class).getRegion()));
        Converter<String, Set<String>> stringSetConverter = Utility.mapConverter(facilityGroup);
        assertThat(stringSetConverter, is(notNullValue()));

        Set<String> actual = stringSetConverter.convert(country);

        Set<String> expected = new HashSet<String>() {{
            add(region);
        }};
        assertThat(actual, is(equalTo(expected)));
    }

    @Test
    public void testReverseKeyValues() {
        HashMap<String, Integer> actual = Utility.reverseKeyValues(new HashMap<Integer, String>() {{
            put(1, "1");
            put(2, "2");
        }});

        HashMap<String, Integer> expected = new HashMap<String, Integer>() {{
            put("1", 1);
            put("2", 2);
        }};
        assertThat(actual, is(equalTo(expected)));
    }
}
