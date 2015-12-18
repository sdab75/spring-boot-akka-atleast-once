package com.ms.common;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.DeadLetter;
import akka.actor.UntypedActor;
import akka.pattern.CircuitBreaker;
import akka.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Parent class of all async event listener implementations.
 * Created by mimenu on 10/30/2015.
 */
@Component
public abstract class NonPersistentActor extends UntypedActor {

/*
    public static final int MAX_FAILURES = 2;
    public static final Timeout ASK_TIMEOUT = Timeout.apply(10, TimeUnit.MILLISECONDS);
    public static final FiniteDuration CALL_TIMEOUT = Duration.create(10, TimeUnit.MILLISECONDS);
    public static final FiniteDuration RESET_TIMEOUT = Duration.create(2, TimeUnit.SECONDS);
*/
public static final int MAX_FAILURES =1;
    public static final Timeout ASK_TIMEOUT = Timeout.apply(40, TimeUnit.MILLISECONDS);
    public static final FiniteDuration CALL_TIMEOUT = Duration.create(100, TimeUnit.MILLISECONDS);
    public static final FiniteDuration RESET_TIMEOUT = Duration.create(2, TimeUnit.SECONDS);

    // private ActorRef service;
    protected CircuitBreaker circuitBreaker;


    public NonPersistentActor(){
        circuitBreaker = new CircuitBreaker(getContext().dispatcher(),
                getContext().system().scheduler(),
                MAX_FAILURES,
                CALL_TIMEOUT,
                RESET_TIMEOUT);

        circuitBreaker.onOpen(new Runnable() {
            public void run() {
                onOpen();
            }
        });

        circuitBreaker.onClose(new Runnable() {
            @Override
            public void run() {
                onClose();
            }
        });

        circuitBreaker.onHalfOpen(new Runnable() {
            @Override
            public void run() {
                onHalfOpen();
            }
        });

    }

    public void onOpen() {
        System.out.println("Abc Circuit Breaker is open ################################");
    }

    public void onClose() {
        System.out.println("Abc Circuit Breaker is closed %%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    }

    public void onHalfOpen() {
        System.out.println("Abc Circuit Breaker is half open, next message will go through $$$$$$$$$$$$$$$$$$$$$$$");
    }

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
