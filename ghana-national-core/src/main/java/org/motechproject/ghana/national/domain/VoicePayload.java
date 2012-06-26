package org.motechproject.ghana.national.domain;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.motechproject.ghana.national.messagegateway.domain.DeliveryStrategy;
import org.motechproject.ghana.national.messagegateway.domain.Payload;
import org.motechproject.util.DateUtil;

public class VoicePayload implements Payload {
    private String uniqueId;
    private DateTime generationTime;
    private DeliveryStrategy deliveryStrategy;
    private Period validity;

    protected VoicePayload() {
    }

    public VoicePayload(String uniqueId, DateTime generationTime, DeliveryStrategy deliveryStrategy, Period validity) {
        this.uniqueId = uniqueId;
        this.generationTime = generationTime;
        this.deliveryStrategy = deliveryStrategy;
        this.validity = validity;
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public DateTime getGenerationTime() {
        return generationTime;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public DateTime getDeliveryTime() {
        return deliveryStrategy.deliveryDate(this);
    }

    @Override
    public Boolean canBeDispatched() {
        return getDeliveryTime().toDate().before(DateUtil.now().toDate());
    }

    public Period getValidity() {
        return validity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VoicePayload that = (VoicePayload) o;

        if (deliveryStrategy != null ? !deliveryStrategy.equals(that.deliveryStrategy) : that.deliveryStrategy != null)
            return false;
        if (generationTime != null ? !generationTime.equals(that.generationTime) : that.generationTime != null)
            return false;
        if (uniqueId != null ? !uniqueId.equals(that.uniqueId) : that.uniqueId != null) return false;
        if (validity != null ? !validity.equals(that.validity) : that.validity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniqueId != null ? uniqueId.hashCode() : 0;
        result = 31 * result + (generationTime != null ? generationTime.hashCode() : 0);
        result = 31 * result + (deliveryStrategy != null ? deliveryStrategy.hashCode() : 0);
        result = 31 * result + (validity != null ? validity.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "VoicePayload{" +
                "uniqueId='" + uniqueId + '\'' +
                ", generationTime=" + generationTime +
                ", deliveryStrategy=" + deliveryStrategy +
                ", validity=" + validity +
                '}';
    }
}
