package com.ms.common;

import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;
import org.springframework.stereotype.Component;

/**
 * Parent class of all async event listener implementations.
 * Created by mimenu on 10/30/2015.
 */

/**
 * Parent class of all async event listener implementations.
 * Created by mimenu on 10/30/2015.
 */
@Component
public abstract class NonPersistentActor extends UntypedActor {

    /**
     * Override to enforce that onReceive is called for EDFEvent and to
     * first propagate security context and disarms it upon completion.
     * @param cmd the cmd to be processed
     */
    public final void onReceive(Object cmd) {
            processReceivedEvent(cmd);
    }

    protected void log(String msg){
        System.out.println(actorName() + " : " + msg);
    }
    /**
     * Process received event.  Must be implemented by subclassed.
     * @param cmd the event to be processed
     */
    protected abstract void processReceivedEvent(Object cmd);
    protected abstract String actorName();
    protected abstract CircuitBreaker getCircuitBreaker();
}
