package com.ms.abc.service;

import akka.actor.*;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ShardRegion;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.ms.common.ClusterMemberManagementUtil;
import com.ms.common.NonPersistentActor;
import com.ms.common.SuperVisorStrategyUtil;
import com.ms.event.AssignmentEvent;
import com.ms.event.EDFEventDeliveryAck;
import com.ms.event.IgnoreErroedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class AbcEventListener extends NonPersistentActor {
    private static final Logger log = LoggerFactory.getLogger(AbcEventListener.class);
/*
    private ActorRef mediator;
*/

    @Autowired
    ActorRef abcEventStoreSupervisorShardRegion;

    @Autowired
    ActorRef initAbcEventStoreSupervisor;

    @Autowired
    ActorSystem actorSystem;

//    final Cluster cluster = Cluster.get(actorSystem);

    private ActorRef caller;

    private int responseTimeout = 100;

    public AbcEventListener() {
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }

    @Override
    public void preStart() throws Exception {
        initSubscriber();
        super.preStart();
        context().setReceiveTimeout(Duration.create(155, SECONDS));
    }

    private void initSubscriber() {
        System.out.println("AbcEventListener init..");
        getContext().watch(initAbcEventStoreSupervisor);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        log("supervision strategy kicked off");
        return SuperVisorStrategyUtil.listenerSupervisorStrategy(actorName(), getSender(), getSelf());
    }


    static final Object Reconnect = "Reconnect";
    static final Object Leave = "leave";

    @Override
    public void postStop() throws Exception {
        super.postStop();
        System.out.println("Listener:  Post Stop called @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }

    @Override
    protected String[] getShardingRegion() {
        String[] regions={"abcListenerShardRegion"};
        return regions ;
    }

    @Override
    public void postRestart(Throwable reason) throws Exception {
        System.out.println("Listener:  Post Restart called @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        super.postRestart(reason);
    }



    @Override
    public void processReceivedEvent(Object msg) {
        if (msg instanceof IgnoreErroedEvent) {
            System.out.println("AbcEventListener Supervisor: Got and forwarding to the persistent actor worker: " + ((AssignmentEvent) msg).getEventName());
            abcEventStoreSupervisorShardRegion.tell(msg, getSelf());
        } else if (msg instanceof AssignmentEvent) {
            System.out.println("AbcEventListener Got: {}" + ((AssignmentEvent) msg).getEventName());
            caller = getSender();
            Future<Object> cbFuture = getCircuitBreaker().callWithCircuitBreaker(new Callable<Future<Object>>() {
                @Override
                public Future<Object> call() throws Exception {
                    return Patterns.ask(abcEventStoreSupervisorShardRegion, msg, Timeout.apply(responseTimeout, TimeUnit.MILLISECONDS));
                }
            });
            Patterns.pipe(cbFuture, getContext().system().dispatcher()).to(getSelf());

        } else if (msg instanceof EDFEventDeliveryAck) {
            System.out.println("AbcEventListener Got : EDFEventDeliveryAck {}" + msg.toString());
            caller.tell(msg, getSelf());
        } else if (msg instanceof DistributedPubSubMediator.SubscribeAck) {
            System.out.println("AbcEventListener: subscribing");
        } else if (msg.equals(ReceiveTimeout.getInstance())) {
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else if (msg instanceof Terminated) {
/*
            cluster.registerOnMemberRemoved(() ->self().tell("member-removed", self()));
            cluster.leave(cluster.selfAddress());
*/
            System.out.println("AbcEventListener:  Termination process kicked, will reconnect after 10 sec");
//            getContext().system().scheduler().scheduleOnce(Duration.create(10, "seconds"), getSelf(), Reconnect, getContext().dispatcher(), null);
        } else if (msg.equals("member-removed")) {
            // Let singletons hand over gracefully before stopping the system
            context().system().scheduler().scheduleOnce(Duration.create(10, SECONDS), self(), "stop-system", context().dispatcher(), self());
            System.out.println("AbcEventListener:  Reconnect process started.");
        } else if (msg.equals("stop-system")) {
            //       actorSystem.terminate();
        } else if (msg.equals(Reconnect)) {
            System.out.println("AbcEventListener:  Reconnect process started.");
            // Re-establish storage after the scheduled delay
            initSubscriber();
        } else if (msg.equals(Leave)) {
            context().watch(initAbcEventStoreSupervisor);
            initAbcEventStoreSupervisor.tell(ShardRegion.gracefulShutdownInstance(), self());
            // Re-establish storage after the scheduled delay
//            initSubscriber();
        } else {
            unhandled(msg);
        }
    }

    @Override
    protected String actorName() {
        return "AbcEventListener";
    }


}