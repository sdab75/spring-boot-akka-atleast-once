package com.ms.def.service;

import akka.actor.*;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import com.ms.config.SpringExtension;
import com.ms.event.AssignmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class DefEventStoreSupervisor extends UntypedActor {
    private static final Logger log= LoggerFactory.getLogger(DefEventStoreSupervisor.class);

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private SupervisorStrategy restartOrEsclate;

    @Autowired
    private Props defEventStoreActorProps;

    private ActorRef eventStoreRef;

    @Override
    public void preStart() throws Exception {
        initActor();
        super.preStart();
    }

    private void initActor() {
        log.info("AbcEvent Store Supervisor Starting up");
        eventStoreRef = getContext().actorOf(defEventStoreActorProps, "defEventStoreActor");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        log.info("WorkerSupervisor: supervisorStrategy invoked #################################################");
        return restartOrEsclate;
    }

    static final Object Reconnect = "Reconnect";

    public void onReceive(Object msg) {
        log.info("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%v " + msg.toString());
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