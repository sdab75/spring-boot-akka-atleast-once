package com.ms.def.service;

import com.ms.common.PersistentActor;
import com.ms.def.rest.EventDispatcher;
import com.ms.event.EDFEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class DefEventStoreActor extends PersistentActor {
    private static final Logger log= LoggerFactory.getLogger(DefEventStoreActor.class);

    @Autowired
    private EventDispatcher eventDispatcher;

    @Override
    public String persistenceId() {
        return "DefEventStoreActor-" + getContext().parent().path().name();
    }


    @Override
    protected boolean validateEvent(EDFEvent edfEvent) {
        return true;
    }

    @Override
    protected void processEvent(EDFEvent edfEvent) {
        System.out.println("Successfully processed event");
    }

    @Override
    protected void publishDoneEvent(EDFEvent edfEvent) {
        if(edfEvent.getEventName().equals(EDFEvent.APPSTATUS.ASSIGNED.name())){
            System.out.println("Status is closed publishing on closed event");
            edfEvent.setEventName(EDFEvent.APPSTATUS.ACQUIRED.name());
            eventDispatcher.dispatchToAbc(edfEvent);
        }

    }
}
