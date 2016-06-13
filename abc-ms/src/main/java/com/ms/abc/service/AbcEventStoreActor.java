package com.ms.abc.service;

import com.ms.abc.rest.EventDispatcher;
import com.ms.common.PersistentActor;
import com.ms.common.RecoverableServiceException;
import com.ms.common.UnRecoverableServiceException;
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
    private static final Logger log = LoggerFactory.getLogger(AbcEventStoreActor.class);

    @Autowired
    private EventDispatcher eventDispatcher;

    @Override
    public String persistenceId() {
        return "AbcEventStoreActor-" + getContext().parent().path().name();
    }

    @Override
    protected String[] getShardingRegion() {
        return null ;
    }


    @Override
    protected String actorName() {
        return "AbcEventStoreActor";
    }

    @Override
    protected boolean validateEvent(EDFEvent edfEvent) {
        return true;
    }

    @Override
    protected void preProcessEvent(EDFEvent edfEvent) {

    }

    int count = 0;

    @Override
    protected void processEvent(EDFEvent edfEvent) {
        if (edfEvent.getEventName().equals(EDFEvent.APPSTATUS.ASSIGNED.name())) {
            if (count == 0) {
                System.out.println("DataStoreException , Counter " + count);
                count++;
                throw new RecoverableServiceException("DataStoreException Intentional Error  !!!!");
            }

            if (count <= 3) {
                System.out.println("ServiceUnavailable, Counter " + count);
                count++;
                throw new UnRecoverableServiceException("ServiceUnavailable Intentional Error !!!!");
            }

            if (count == 4) {
                System.out.println("RuntimeException, Counter " + count);
                count++;
                throw new RuntimeException("RuntimeException Intentional Error !!!!");
            }

            if (count == 5) {
                System.out.println("Processing ... " + count);
                count = 0;
            }
        }
        System.out.println("AbcEventStoreActor ===> Successfully processed event");
    }

    @Override
    protected void postProcessEvent(EDFEvent edfEvent) {

    }

    @Override
    protected EDFEvent publishDoneEvent(EDFEvent edfEvent) {
        if (edfEvent.getEventName().equals(EDFEvent.APPSTATUS.ACQUIRED.name())) {
            System.out.println("Publishing ACQUIRED event");
            edfEvent.setEventName(EDFEvent.STATUS.INPROGRESS.name());
            eventDispatcher.dispatchToDef(edfEvent);
        }
        return edfEvent;
    }
}
