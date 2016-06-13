package com.ms.event;

import java.io.Serializable;

/**
 * Created by davenkat on 11/12/2015.
 */
public class IgnoreErroedEvent implements Serializable {

    public StoredEvent getStoredEvent() {
        return storedEvent;
    }

    public void setStoredEvent(StoredEvent storedEvent) {
        this.storedEvent = storedEvent;
    }

    private StoredEvent storedEvent;
    public IgnoreErroedEvent(StoredEvent storedEvent){
        this.storedEvent=storedEvent;
    }


}
