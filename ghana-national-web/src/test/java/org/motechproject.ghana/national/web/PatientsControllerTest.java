package org.motechproject.ghana.national.web;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.motechproject.ghana.national.service.FacilityService;
import org.springframework.ui.ModelMap;

import java.util.HashMap;

import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientsControllerTest {
    PatientsController patientsController;
    @Mock
    FacilityService mockFacilityService;

    @Before
    public void setUp() {
        initMocks(this);
        patientsController = new PatientsController(mockFacilityService);
    }

    @Test
    public void shouldRenderPageToCreateAPatient() {
        final ModelMap modelMap = new ModelMap();
        final String key = "key";
        when(mockFacilityService.locationMap()).thenReturn(new HashMap<String, Object>() {{
            put(key, new Object());
        }});
        patientsController.newPatientForm(modelMap);
        verify(mockFacilityService).locationMap();
        assertNotNull(modelMap.get(key));
    }
}
