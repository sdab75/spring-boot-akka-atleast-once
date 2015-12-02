package com.ms.def.service;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import com.ms.event.AssignmentEvent;
import com.ms.event.EDFEventDeliveryAck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class DefEventListener extends UntypedActor {
    private static final Logger log= LoggerFactory.getLogger(DefEventListener.class);
    @Autowired
    private SupervisorStrategy restartOrEsclate;

    private ActorRef mediator;

    @Autowired
    ActorRef defEventStoreSupervisorShardRegion;

    @Override
    public void preStart() throws Exception {
        initSubscriber();
        super.preStart();
        context().setReceiveTimeout(Duration.create(155, TimeUnit.SECONDS));
    }
    private void initSubscriber(){
        System.out.println("Starting up");
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return restartOrEsclate;
    }

    public DefEventListener() {
        //Testing copy
        mediator = DistributedPubSub.get(getContext().system()).mediator();
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }
    static final Object Reconnect = "Reconnect";

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("Listener:  Post Stop called @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("Listener:  Post Restart called @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.postRestart(reason);
    }

    @Override
    public void onReceive(Object msg) {
        log.info("Subscriber Got: {}"+  msg.toString());
        if (msg instanceof AssignmentEvent) {
            log.info("Subscriber Got: {}" + ((AssignmentEvent) msg).getEventName());
            defEventStoreSupervisorShardRegion.tell(msg, getSelf());
            getSender().tell(new EDFEventDeliveryAck(((AssignmentEvent) msg).getEventDeliveryId()), getSelf());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            log.info("Listener: subscribing");
        } else if (msg.equals(ReceiveTimeout.getInstance())){
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        }else if (msg instanceof Terminated) {
            System.out.println("Listener:  Termination process kicked, will reconnect after 10 sec");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect,getContext().dispatcher(), null);
        }else if (msg.equals(Reconnect)) {
            System.out.println("Listener:  Reconnect process started.");
            // Re-establish storage after the scheduled delay
            initSubscriber();
        }else {
            unhandled(msg);
        }
    }
}