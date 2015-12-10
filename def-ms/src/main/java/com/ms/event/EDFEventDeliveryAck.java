package com.ms.event;

import java.io.Serializable;

/**
 * Created by davenkat on 11/30/2015.
 */
public class EDFEventDeliveryAck implements Serializable{
    private long eventDeliveryId;
    private boolean successfull;
    public EDFEventDeliveryAck(long eventDeliveryId, boolean successfull){
        this.eventDeliveryId=eventDeliveryId;
        this.successfull = successfull;
    }

    public long getEventDeliveryId() {
        return eventDeliveryId;
    }

    public void setEventDeliveryId(long eventDeliveryId) {
        this.eventDeliveryId = eventDeliveryId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("EDFEventDeliveryAck{");
        sb.append("eventDeliveryId=").append(eventDeliveryId);
        sb.append('}');
        return sb.toString();
    }

    public boolean isSuccessfull() {
        return successfull;
    }
}

