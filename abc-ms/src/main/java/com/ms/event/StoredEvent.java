package com.ms.event;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by davenkat on 11/12/2015.
 */
public class StoredEvent implements Serializable {

    public StoredEvent(){

    }

    public StoredEvent (EDFEvent EDFEvent){
        this.EDFEvent=EDFEvent;
        lastUpdated= new Date();

    }
    public EDFEvent getEDFEvent() {
        return EDFEvent;
    }

    public void setEDFEvent(EDFEvent EDFEvent) {
        this.EDFEvent = EDFEvent;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    private EDFEvent EDFEvent;
    private Date lastUpdated;


}
