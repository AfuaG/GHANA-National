package org.motechproject.ghana.national.handlers;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.motechproject.ghana.national.bean.ANCVisitForm;
import org.motechproject.ghana.national.service.ANCVisitService;
import org.motechproject.ghana.national.vo.ANCVisit;
import org.motechproject.model.MotechEvent;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class ANCVisitFormHandlerTest {

    private ANCVisitFormHandler handler;
    @Mock
    private ANCVisitService visitService;


    @Before
    public void setUp() {
        initMocks(this);
        handler = new ANCVisitFormHandler();
        ReflectionTestUtils.setField(handler, "visitService", visitService);

    }

    @Test
    public void shouldCreateANCVisitEncounterWithAllInfo() {
        final ANCVisitForm ancVisitForm = new ANCVisitForm();
        ancVisitForm.setStaffId("465");
        ancVisitForm.setFacilityId("232465");
        ancVisitForm.setMotechId("2321465");
        ancVisitForm.setDate(new Date());
        ancVisitForm.setSerialNumber("4ds65");
        ancVisitForm.setVisitNumber("4");
        ancVisitForm.setEstDeliveryDate(new Date(2012, 8, 8));
        ancVisitForm.setBpDiastolic(67);
        ancVisitForm.setBpSystolic(10);
        ancVisitForm.setWeight(65.67d);
        ancVisitForm.setComments("comments");
        ancVisitForm.setTtdose("4");
        ancVisitForm.setIptdose("56");
        ancVisitForm.setIptReactive("reactive");
        ancVisitForm.setItnUse("itn");
        ancVisitForm.setFht(4.3d);
        ancVisitForm.setFhr(4);
        ancVisitForm.setUrineTestGlucosePositive("glucose");
        ancVisitForm.setUrineTestProteinPositive("protein");
        ancVisitForm.setHemoglobin(13.8);
        ancVisitForm.setVdrlReactive("vdrl");
        ancVisitForm.setVdrlTreatment("treament");
        ancVisitForm.setDewormer("dewormer");
        ancVisitForm.setPmtct("pmctc");
        ancVisitForm.setPreTestCounseled("pre-test");
        ancVisitForm.setHivTestResult("hiv");
        ancVisitForm.setPostTestCounseled("post");
        ancVisitForm.setPmtctTreament("treatment");
        ancVisitForm.setLocation("location");
        ancVisitForm.setHouse("house");
        ancVisitForm.setCommunity("community");
        ancVisitForm.setReferred("referred");
        ancVisitForm.setMaleInvolved(false);
        ancVisitForm.setNextANCDate(new Date(2012, 2, 20));


        MotechEvent motechEvent = new MotechEvent("form.validation.successful.NurseDataEntry.ancVisit", new HashMap<String, Object>() {{
            put("formBean", ancVisitForm);
        }});


        handler.handleFormEvent(motechEvent);

        ArgumentCaptor<ANCVisit> captor = ArgumentCaptor.forClass(ANCVisit.class);
        verify(visitService).registerANCVisit(captor.capture());
        ANCVisit actualANCVisit = captor.getValue();
        assertEquals(ancVisitForm.getStaffId(), actualANCVisit.getStaffId());
        assertEquals(ancVisitForm.getFacilityId(), actualANCVisit.getFacilityId());
        assertEquals(ancVisitForm.getMotechId(), actualANCVisit.getMotechId());
        assertEquals(ancVisitForm.getDate(), actualANCVisit.getDate());
        assertEquals(ancVisitForm.getSerialNumber(), actualANCVisit.getSerialNumber());
        assertEquals(ancVisitForm.getVisitNumber(), actualANCVisit.getVisitNumber());
        assertEquals(ancVisitForm.getEstDeliveryDate(), actualANCVisit.getEstDeliveryDate());
        assertEquals(ancVisitForm.getBpDiastolic(), actualANCVisit.getBpDiastolic());
        assertEquals(ancVisitForm.getBpSystolic(), actualANCVisit.getBpSystolic());
        assertEquals(ancVisitForm.getWeight(), actualANCVisit.getWeight());
        assertEquals(ancVisitForm.getComments(), actualANCVisit.getComments());
        assertEquals(ancVisitForm.getTtdose(), actualANCVisit.getTtdose());
        assertEquals(ancVisitForm.getIptdose(), actualANCVisit.getIptdose());
        assertEquals(ancVisitForm.getIptReactive(), actualANCVisit.getIptReactive());
        assertEquals(ancVisitForm.getItnUse(), actualANCVisit.getItnUse());
        assertEquals(ancVisitForm.getFht(), actualANCVisit.getFht());
        assertEquals(ancVisitForm.getFhr(), actualANCVisit.getFhr());
        assertEquals(ancVisitForm.getUrineTestGlucosePositive(), actualANCVisit.getUrineTestGlucosePositive());
        assertEquals(ancVisitForm.getUrineTestProteinPositive(), actualANCVisit.getUrineTestProteinPositive());
        assertEquals(ancVisitForm.getHemoglobin(), actualANCVisit.getHemoglobin());
        assertEquals(ancVisitForm.getVdrlReactive(), actualANCVisit.getVdrlReactive());
        assertEquals(ancVisitForm.getVdrlTreatment(), actualANCVisit.getVdrlTreatment());
        assertEquals(ancVisitForm.getDewormer(), actualANCVisit.getDewormer());
        assertEquals(ancVisitForm.getPmtct(), actualANCVisit.getPmtct());
        assertEquals(ancVisitForm.getPreTestCounseled(), actualANCVisit.getPreTestCounseled());
        assertEquals(ancVisitForm.getHivTestResult(), actualANCVisit.getHivTestResult());
        assertEquals(ancVisitForm.getPostTestCounseled(), actualANCVisit.getPostTestCounseled());
        assertEquals(ancVisitForm.getPmtctTreament(), actualANCVisit.getPmtctTreament());
        assertEquals(ancVisitForm.getLocation(), actualANCVisit.getLocation());
        assertEquals(ancVisitForm.getHouse(), actualANCVisit.getHouse());
        assertEquals(ancVisitForm.getCommunity(), actualANCVisit.getCommunity());
        assertEquals(ancVisitForm.getReferred(), actualANCVisit.getReferred());
        assertEquals(ancVisitForm.getMaleInvolved(), actualANCVisit.getMaleInvolved());
        assertEquals(ancVisitForm.getNextANCDate(), actualANCVisit.getNextANCDate());
    }
}
