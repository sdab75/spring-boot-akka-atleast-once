package com.ms.abc.service;

import akka.pattern.CircuitBreaker;
import akka.util.Timeout;
import com.ms.common.CircuitBreakerUtil;
import com.ms.common.DistEventSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class AbcToDefDistEventSender extends DistEventSender {
    @Autowired
    CircuitBreakerUtil circuitBreakerUtil;

    private int maxFailures=2;
    private int responseTimeout=100;
    private int callFailureTimeout=100;
    private int resetTimeout=20;

    @Override
    protected String actorName() {
        return "AbcToDefDistEventSender";
    }

    @Override
    protected String destinationPath() {
        return "/user/defEventListener";
    }

    @Override
    protected String senderAckPath() {
        return "/user/abcToDefEventSender";
    }

    @Override
    protected Timeout getResponseTime() {
        return Timeout.apply(responseTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    protected CircuitBreaker getCircuitBreaker() {
        return circuitBreakerUtil.getCircuitBreaker(actorName(), getContext().dispatcher(),
                getContext().system().scheduler(), maxFailures,
                Duration.create(responseTimeout, TimeUnit.MILLISECONDS),
                Duration.create(resetTimeout, TimeUnit.SECONDS));
    }
}
