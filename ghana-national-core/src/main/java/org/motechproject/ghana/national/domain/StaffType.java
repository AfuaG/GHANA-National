package org.motechproject.ghana.national.domain;

import org.codehaus.jackson.annotate.JsonProperty;
import org.ektorp.support.TypeDiscriminator;
import org.motechproject.model.MotechBaseDataObject;

import static java.util.Arrays.asList;


@TypeDiscriminator("doc.type === 'StaffType'")
public class StaffType extends MotechBaseDataObject {
    @JsonProperty("type")
    private String type = "StaffType";
    @JsonProperty
    private String name;
    @JsonProperty
    private String description;

    public StaffType() {
    }

    public StaffType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public enum Role {
   /*     CALL_CENTER_ADMIN("CallCenter Admin", Constants.SECURITY_ROLE_PROVIDER),
        COMMUNITY_HEALTH_NURSE("CHN", Constants.SECURITY_ROLE_PROVIDER),
        COMMUNITY_HEALTH_OPERATOR("CHO", Constants.SECURITY_ROLE_PROVIDER),
        COMMUNITY_HEALTH_VOLUNTEER("CHV", Constants.SECURITY_ROLE_PROVIDER),
        MOTECH_FIELD_AGENT("FA", Constants.SECURITY_ROLE_PROVIDER),
        FACILITY_ADMIN("Facility Admin", Constants.SECURITY_ROLE_PROVIDER),
        HEALTH_CARE_ADMIN("HealthCare Admin", Constants.SECURITY_ROLE_PROVIDER),
        HEALTH_EXTENSION_WORKER("HEW", Constants.SECURITY_ROLE_PROVIDER),
        HEALTH_PROMOTION_OFFICER("HPO", Constants.SECURITY_ROLE_PROVIDER),
        MOBILE_MIDWIFE_AGENT("MMA", Constants.SECURITY_ROLE_PROVIDER), */

        //TODO: All roles will have super admin privileges till staff role login is played <geet/balaji>

        CALL_CENTER_ADMIN("CallCenter Admin", Constants.SECURITY_ROLE_SUPER),
        COMMUNITY_HEALTH_NURSE("CHN", Constants.SECURITY_ROLE_SUPER),
        COMMUNITY_HEALTH_OPERATOR("CHO", Constants.SECURITY_ROLE_SUPER),
        COMMUNITY_HEALTH_VOLUNTEER("CHV", Constants.SECURITY_ROLE_SUPER),
        MOTECH_FIELD_AGENT("FA", Constants.SECURITY_ROLE_SUPER),
        FACILITY_ADMIN("Facility Admin", Constants.SECURITY_ROLE_SUPER),
        HEALTH_CARE_ADMIN("HealthCare Admin", Constants.SECURITY_ROLE_SUPER),
        HEALTH_EXTENSION_WORKER("HEW", Constants.SECURITY_ROLE_SUPER),
        HEALTH_PROMOTION_OFFICER("HPO", Constants.SECURITY_ROLE_SUPER),
        MOBILE_MIDWIFE_AGENT("MMA", Constants.SECURITY_ROLE_SUPER),
        SUPER_ADMIN("Super Admin", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_MIDWIFE("Midwife", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_ASSISTANT_MIDWIFE("Assistant Midwife", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_PRINCIPAL_NURSING_OFFICER("Principal Nursing Officer", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_MEDICAL_SUPERINTENDENT("Medical Superintendent", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_WARD_ASSISTANT("Ward Assistant", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_NURSE("Nurse", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_NURSE_AID("Nurse Aid", Constants.SECURITY_ROLE_SUPER),
        PRIVATE_HOSPITAL_HEALTH_AID("Health Aid", Constants.SECURITY_ROLE_SUPER);

        private String key;
        private String securityRole;

        Role(String key, String securityRole) {
            this.key = key;
            this.securityRole = securityRole;
        }

        public static String securityRoleFor(String key) {
            for (Role role : values())
                if (role.key.equalsIgnoreCase(key)) return role.securityRole;
            return Constants.SECURITY_ROLE_PROVIDER;
        }

        public static Boolean isAdmin(String roleName) {
            return asList(SUPER_ADMIN.key(), FACILITY_ADMIN.key(), CALL_CENTER_ADMIN.key(), HEALTH_CARE_ADMIN.key()).contains(roleName);
        }

        public String key() {
            return key;
        }
    }
}

