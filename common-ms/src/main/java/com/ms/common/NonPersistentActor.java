package com.ms.common;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.ClusterEvent.*;
import akka.cluster.Member;
import akka.cluster.sharding.ClusterSharding;
import akka.pattern.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

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
    Cluster cluster = Cluster.get(getContext().system());

    //subscribe to cluster changes
    @Override
    public void preStart() throws Exception {
        ActorSystem system = getContext().system();
        //#subscribe
        cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), MemberEvent.class, UnreachableMember.class, MemberUp.class, MemberRemoved.class);
        ClusterMemberManagementUtil.watchShardRegion(getContext(), getSelf(),actorName(), getShardingRegion());

    }

    @Override
    public void postStop() throws Exception {
        cluster.unsubscribe(getSelf());
    }

    abstract protected String[] getShardingRegion();


    /**
     * Override to enforce that onReceive is called for EDFEvent and to
     * first propagate security context and disarms it upon completion.
     *
     * @param message the cmd to be processed
     */
    public final void onReceive(Object message) {
        if (message instanceof ClusterEvent.MemberRemoved) {
            System.out.println(actorName()+ " cleaning Sharding Coordinators ======>" + message.toString());
            ClusterMemberManagementUtil.cleanShardingCoordinators(message, getContext(),getSelf(),actorName(),getShardingRegion());
        } else
            processReceivedEvent(message);
    }

    protected void log(String msg) {
        System.out.println(actorName() + " : " + msg);
    }

    /**
     * Process received event.  Must be implemented by subclassed.
     *
     * @param cmd the event to be processed
     */
    protected abstract void processReceivedEvent(Object cmd);

    protected abstract String actorName();


    @Autowired
    CircuitBreakerUtil circuitBreakerUtil;

    private int maxFailures = 2;
    private int responseTimeout = 100;
    private int callFailureTimeout = 100;
    private int resetTimeout = 20;

    protected CircuitBreaker getCircuitBreaker() {
        return circuitBreakerUtil.getCircuitBreaker(actorName(), getContext().dispatcher(),
                getContext().system().scheduler(), maxFailures,
                Duration.create(responseTimeout, TimeUnit.MILLISECONDS),
                Duration.create(resetTimeout, TimeUnit.SECONDS));
    }

}
