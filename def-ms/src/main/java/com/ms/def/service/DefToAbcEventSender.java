package com.ms.def.service;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.cluster.sharding.ClusterSharding;
import com.ms.common.AtleastOnceEventSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Created by davenkat on 11/30/2015.
 */
@Component
@Scope("prototype")
public class DefToAbcEventSender extends AtleastOnceEventSender {
    @Override
    public String persistenceId() {
        return "DefToAbcEventSender-" + getContext().parent().path().name();
    }


    @Override
    protected ActorRef destinationActorPath() {
        return ClusterSharding.get(getContext().system()).shardRegion("defToAbcDistEventSender");
    }


    @Override
    protected String actorName() {
        return "DefToAbcEventSender";
    }
}
