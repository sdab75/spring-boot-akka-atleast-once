package com.ms.abc.service;

import com.ms.abc.rest.EventDispatcher;
import com.ms.common.PersistentActor;
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
public class AbcEventStoreActor extends PersistentActor {
    private static final Logger log= LoggerFactory.getLogger(AbcEventStoreActor.class);

    @Autowired
    private EventDispatcher eventDispatcher;

    @Override
    public String persistenceId() {
        return "AbcEventStoreActor-" + getContext().parent().path().name();
    }


    @Override
    protected boolean validateEvent(EDFEvent edfEvent) {
        return true;
    }

    @Override
    protected void processEvent(EDFEvent edfEvent) {
        if(edfEvent.getEventName().equals(EDFEvent.APPSTATUS.ASSIGNED.name())){
            throw new RuntimeException("Intentional Error !!!!");
        }

        System.out.println("Successfully processed event");
    }

    @Override
    protected void publishDoneEvent(EDFEvent edfEvent) {
/*
        if(edfEvent.getEventName().equals(EDFEvent.APPSTATUS.ACQUIRED.name())){
            System.out.println("Publishing ACQUIRED event");
            edfEvent.setEventName(EDFEvent.STATUS.INPROGRESS.name());
            eventDispatcher.dispatchToDef(edfEvent);
        }
*/
    }
}
