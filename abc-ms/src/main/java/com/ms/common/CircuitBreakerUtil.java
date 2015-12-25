package com.ms.common;

import akka.actor.Scheduler;
import akka.pattern.CircuitBreaker;
import org.springframework.stereotype.Component;
import scala.concurrent.ExecutionContext;
import scala.concurrent.duration.FiniteDuration;

import java.util.logging.Logger;

/**
 * Created by davenkat on 12/17/2015.
 */
@Component
public class CircuitBreakerUtil {

    public CircuitBreaker getCircuitBreaker(String actorName, ExecutionContext executionContext, Scheduler scheduler, int maxFailures, FiniteDuration callTimeout, FiniteDuration resetTimeout) {
        CircuitBreaker circuitBreaker = new CircuitBreaker(executionContext, scheduler,
                maxFailures,
                callTimeout,
                resetTimeout);

        circuitBreaker.onOpen(new Runnable() {
            public void run() {
                onOpen(actorName);
            }
        });

        circuitBreaker.onClose(new Runnable() {
            @Override
            public void run() {
                onClose(actorName);
            }
        });

        circuitBreaker.onHalfOpen(new Runnable() {
            @Override
            public void run() {
                onHalfOpen(actorName);
            }
        });

        return circuitBreaker;
    }

    public void onOpen(String actorName) {
        log(actorName, "Circuit Breaker is open ################################");
    }

    public void onClose(String actorName) {
        log(actorName, "Circuit Breaker is closed %%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    }

    public void onHalfOpen(String actorName) {
        log(actorName, "Circuit Breaker is half open, next message will go through $$$$$$$$$$$$$$$$$$$$$$$");
    }

    protected void log(String actorName, String msg) {
        System.out.println(actorName + " : " + msg);
    }
}
