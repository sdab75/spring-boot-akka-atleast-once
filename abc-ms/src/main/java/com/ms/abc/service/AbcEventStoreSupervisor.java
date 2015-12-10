package com.ms.abc.service;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Function;
import com.ms.common.WrapperException;
import com.ms.config.SpringExtension;
import com.ms.event.AssignmentEvent;
import com.ms.event.EDFEvent;
import com.ms.event.IgnoreErroedEvent;
import com.ms.event.StoredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import static akka.actor.SupervisorStrategy.*;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class AbcEventStoreSupervisor extends UntypedActor {
    private static final Logger log= LoggerFactory.getLogger(AbcEventStoreSupervisor.class);

    @Autowired
    private SpringExtension springExtension;

/*
    @Autowired
    private SupervisorStrategy restartOrEsclate;
*/

    @Autowired
    private Props abcEventStoreActorProps;

    private ActorRef eventStoreRef;

    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    private void initActor() {
        log.info("AbcEvent Store Supervisor Starting up");
        eventStoreRef = getContext().watch(getContext().actorOf(abcEventStoreActorProps, "abcEventStoreActor"));
    }

    public SupervisorStrategy restartOrEsclate() {
        SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public SupervisorStrategy.Directive apply(Throwable th) {
                if (th instanceof WrapperException) {
                    System.out.println("AbcEventStoreSupervisor supervisor strategy called");
                    Throwable t = th.getCause();
                    if (t instanceof ServiceUnavailable) {
                        System.out.println("oneToOne: restartOrEsclate strategy, escalate");
                        return escalate();
                    } else if (t instanceof DataStoreException) {
                        System.out.println("oneToOne: DataStoreException invoked, escalating to oneToAll @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                        return restart();
                    }else {
                        System.out.println("AbcEventStoreSupervisor supervisor strategy: final else called sending ignore event");
                        getSender().tell(new IgnoreErroedEvent((StoredEvent) ((WrapperException) th).getObj()), getSelf());
                        return resume();
                    }
                } else {
                    System.out.println("oneToOne: final else called escalating to oneToAll");
                    return resume();
                }
            }
        });
        return strategy;
    }


    @Override
    public SupervisorStrategy supervisorStrategy() {
        log.info("WorkerSupervisor: supervisorStrategy invoked #################################################");
        return restartOrEsclate();
    }

    static final Object Reconnect = "Reconnect";

    public void onReceive(Object msg) {
        log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%v " + msg.toString());
        if (msg instanceof IgnoreErroedEvent) {
            log.info("Worker Supervisor: Got and forwarding to the persistent actor worker: ", ((AssignmentEvent) msg).getEventName());
            eventStoreRef.forward(msg, getContext());
        }else
        if (msg instanceof AssignmentEvent) {
            log.info("Worker Supervisor: Got and forwarding to the persistent actor worker: ", ((AssignmentEvent) msg).getEventName());
            eventStoreRef.forward(msg, getContext());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("ListenerSupervisor subscribing");
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
}