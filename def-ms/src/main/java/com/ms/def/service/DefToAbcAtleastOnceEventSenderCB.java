package com.ms.def.service;

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
public class DefToAbcAtleastOnceEventSenderCB extends AtleastOnceEventSenderCB {
    private int responseTimeout=100;

    @Autowired
    private ActorRef abcListenerShardRegionProxy;

    @Override
    protected String[] getShardingRegion() {
        return null ;
    }
    @Override
    protected String actorName() {
        return "DefToAbcAtleastOnceEventSenderCB";
    }

    @Override
    protected ActorRef destinationPath() {
       return abcListenerShardRegionProxy;
    }


    @Override
    protected Timeout getResponseTime() {
        return Timeout.apply(responseTimeout, TimeUnit.MILLISECONDS);
    }
}
