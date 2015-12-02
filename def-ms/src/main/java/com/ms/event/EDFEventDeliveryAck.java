package com.ms.event;

import java.io.Serializable;

/**
 * Created by davenkat on 11/30/2015.
 */
public class EDFEventDeliveryAck implements Serializable{
    long eventDeliveryId;

    public EDFEventDeliveryAck(long eventDeliveryId){
        this.eventDeliveryId=eventDeliveryId;
    }

    public long getEventDeliveryId() {
        return eventDeliveryId;
    }

    public void setEventDeliveryId(long eventDeliveryId) {
        this.eventDeliveryId = eventDeliveryId;
    }
}
