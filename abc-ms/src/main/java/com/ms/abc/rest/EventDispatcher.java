package com.ms.abc.rest;

import akka.actor.ActorRef;
import com.ms.event.EDFEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 10/10/2015.
 */
@Component
public class EventDispatcher {
    @Autowired
    ActorRef abcToDefEventSenderShardRegion;

    public void dispatchToDef(EDFEvent assignmentEvent) {
        abcToDefEventSenderShardRegion.tell(assignmentEvent, null);
    }
}
