package com.ms.common;

import akka.actor.UntypedActor;
import org.springframework.stereotype.Component;

/**
 * Parent class of all async event listener implementations.
 * Created by mimenu on 10/30/2015.
 */
@Component
public abstract class NonPersistentActor extends UntypedActor {
    /**
     * Override to enforce that onReceive is called for BaseEDFEvents and to
     * first propagate security context and disarms it upon completion.
     *
     * @param cmd the cmd to be processed
     */
    @Override
    public void onReceive(Object cmd) {
        processReceivedEvent(cmd);
    }

    /**
     * Process received event.  Must be implemented by subclassed.
     *
     * @param cmd the event to be processed
     */
    protected abstract void processReceivedEvent(Object cmd);
}
