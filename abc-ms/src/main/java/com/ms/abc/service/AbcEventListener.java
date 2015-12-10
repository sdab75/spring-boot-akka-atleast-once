package com.ms.abc.service;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Function;
import com.ms.common.WrapperException;
import com.ms.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

import static akka.actor.SupervisorStrategy.*;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class AbcEventListener extends UntypedActor {
    private static final Logger log = LoggerFactory.getLogger(AbcEventListener.class);
/*    @Autowired
    private SupervisorStrategy restartOrEsclate;*/

    private ActorRef mediator;

    @Autowired
    ActorRef abcEventStoreSupervisorShardRegion;

    @Autowired
    ActorRef initAbcEventStoreSupervisor;

    private ActorRef caller;

    @Autowired
    Props abcEventListenerProps;

    @Override
    public void preStart() throws Exception {
        initSubscriber();
        super.preStart();
        context().setReceiveTimeout(Duration.create(155, TimeUnit.SECONDS));
    }

    private void initSubscriber() {
        System.out.println("AbcEventListener init..");
        getContext().watch(initAbcEventStoreSupervisor);
    }

    public SupervisorStrategy restartOrEsclate() {
        SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, SupervisorStrategy.Directive>() {
            @Override
            public SupervisorStrategy.Directive apply(Throwable th) {
                if (th instanceof WrapperException) {
                    Throwable t = th.getCause();
                    if (t instanceof ServiceUnavailable) {
                        System.out.println("oneToOne: restartOrEsclate strategy, escalate");
                        return escalate();
                    } else if (t instanceof DataStoreException) {
                        System.out.println("oneToOne: DataStoreException invoked, escalating to oneToAll @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                        return restart();
                    }else {
                        System.out.println("oneToOne: restartOrEsclate strategy, escalate");
                        getSender().tell(new IgnoreErroedEvent((StoredEvent) ((WrapperException) th).getObj()), getSelf());
                        return resume();
                    }
                } else {
                    System.out.println("AbcEventListener supervisor strategy: final else called escalating to oneToAll");
                    return resume();
                }
            }
        });
        return strategy;
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        System.out.println("Listener:  supervision strategy kicked off");
        return restartOrEsclate();
    }

    public AbcEventListener() {
        //Testing copy
        mediator = DistributedPubSub.get(getContext().system()).mediator();
/*
        mediator.tell(new DistributedPubSubMediator.Subscribe("contentAbc", "abcEventGroup", getSelf()), getSelf());
*/
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
        System.out.println("Subscriber Got: {}" + msg.toString());
        if (msg instanceof IgnoreErroedEvent) {
            System.out.println("Worker Supervisor: Got and forwarding to the persistent actor worker: " + ((AssignmentEvent) msg).getEventName());
            abcEventStoreSupervisorShardRegion.tell(msg, getSelf());
        } else if (msg instanceof AssignmentEvent) {
            System.out.println("Subscriber Got: {}" + ((AssignmentEvent) msg).getEventName());
            abcEventStoreSupervisorShardRegion.tell(msg, getSelf());
            caller = getSender();
        } else if (msg instanceof EDFEventDeliveryAck) {
            System.out.println("Subscriber Got : EDFEventDeliveryAck {}" + msg.toString());
            caller.tell(msg, getSelf());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            System.out.println("Listener: subscribing");
        } else if (msg.equals(ReceiveTimeout.getInstance())) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else if (msg instanceof Terminated) {
            System.out.println("Listener:  Termination process kicked, will reconnect after 10 sec");
            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect, getContext().dispatcher(), null);
        } else if (msg.equals(Reconnect)) {
            System.out.println("Listener:  Reconnect process started.");
            // Re-establish storage after the scheduled delay
            initSubscriber();
        } else {
            unhandled(msg);
        }
    }
}