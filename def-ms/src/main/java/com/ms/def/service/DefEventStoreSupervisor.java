package com.ms.def.service;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.pattern.CircuitBreaker;
import com.ms.common.CircuitBreakerUtil;
import com.ms.common.NonPersistentActor;
import com.ms.common.SuperVisorStrategyUtil;
import com.ms.config.SpringExtension;
import com.ms.event.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class DefEventStoreSupervisor extends NonPersistentActor {
    private static final Logger log= LoggerFactory.getLogger(DefEventStoreSupervisor.class);

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private Props defEventStoreActorProps;

    private ActorRef eventStoreRef;

    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    private void initActor() {
        System.out.println("AbcEvent Store Supervisor Starting up");
        eventStoreRef = getContext().actorOf(defEventStoreActorProps, "defEventStoreActor");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        log("supervision strategy kicked off");
        return SuperVisorStrategyUtil.persistentActorSupervisorStrategy(actorName(), getSender(), getSelf());
    }
    @Override
    protected String[] getShardingRegion() {
        String[] regions={"defEventStoreSupervisor"};
        return regions ;
    }


    static final Object Reconnect = "Reconnect";

    @Override
    protected void processReceivedEvent(Object msg) {
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%v " + msg.toString());
        if (msg instanceof AssignmentEvent) {
            System.out.println("Worker Supervisor: Got and forwarding to the persistent actor worker: "+((AssignmentEvent) msg).getEventName());
            eventStoreRef.forward(msg, getContext());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            System.out.println("ListenerSupervisor subscribing");
        } else if (msg == ReceiveTimeout.getInstance()) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
            // No progress within 15 seconds, ServiceUnavailable
            log.error("ListenerSupervisor: Timeout kicked, Shutting down due to unavailable service");
            getContext().system().terminate();
        } else if (msg instanceof Terminated) {
            System.out.println("ListenerSupervisor: Termination kicked !!!!!!!!!!!!!!!");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect, getContext().dispatcher(), null);
        } else if (msg.equals(Reconnect)) {
            System.out.println("ListenerSupervisor: Reconnect process started !!!!!!!!!!!!!!!");
            // Re-establish storage after the scheduled delay
            initActor();
        } else {
            unhandled(msg);
        }
    }

    @Override
    protected String actorName() {
        return "DefEventStoreSupervisor";
    }
    @Autowired
    CircuitBreakerUtil circuitBreakerUtil;
    private int maxFailures=2;
    private int responseTimeout=100;
    private int callFailureTimeout=100;
    private int resetTimeout=20;

    @Override
    protected CircuitBreaker getCircuitBreaker() {
        return circuitBreakerUtil.getCircuitBreaker(actorName(), getContext().dispatcher(),
                getContext().system().scheduler(), maxFailures,
                Duration.create(responseTimeout, TimeUnit.MILLISECONDS),
                Duration.create(resetTimeout, TimeUnit.SECONDS));
    }
}