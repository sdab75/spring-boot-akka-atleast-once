package com.ms.abc.service;

import akka.actor.ActorRef;
import akka.pattern.CircuitBreaker;
import akka.util.Timeout;
import com.ms.common.CircuitBreakerUtil;
import com.ms.common.AtleastOnceEventSenderCB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class AbcToDefAtleastOnceEventSenderCB extends AtleastOnceEventSenderCB {
    private int responseTimeout=100;

    @Autowired
    private ActorRef defListenerShardRegionProxy;

    @Override
    protected String[] getShardingRegion() {
        return new String[0];
    }

    @Override
    protected String actorName() {
        return "AbcToDefAtleastOnceEventSenderCB";
    }

    @Override
    protected ActorRef destinationPath() {
        return defListenerShardRegionProxy;
    }

    @Override
    protected Timeout getResponseTime() {
        return Timeout.apply(responseTimeout, TimeUnit.MILLISECONDS);
    }

}
