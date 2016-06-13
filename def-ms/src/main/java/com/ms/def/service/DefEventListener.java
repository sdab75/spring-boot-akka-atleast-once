package com.ms.def.service;

import akka.actor.*;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.pattern.Patterns;
import akka.util.Timeout;
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

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
@Scope("prototype")
public class DefEventListener extends NonPersistentActor {
    private static final Logger log = LoggerFactory.getLogger(DefEventListener.class);


    private ActorRef mediator;
    private ActorRef caller;

    @Autowired
    ActorRef defEventStoreSupervisorShardRegion;

    @Autowired
    ActorRef initdefEventStoreSupervisor;

    private int responseTimeout = 100;

    @Override
    public void preStart() throws Exception {
        initSubscriber();
        super.preStart();
        context().setReceiveTimeout(Duration.create(155, TimeUnit.SECONDS));
    }

    private void initSubscriber() {
        System.out.println("AbcEventListener init..");
        getContext().watch(initdefEventStoreSupervisor);
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        log("supervision strategy kicked off");
        return SuperVisorStrategyUtil.listenerSupervisorStrategy(actorName(), getSender(), getSelf());
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
    protected String[] getShardingRegion() {
        String[] regions={"defListenerShardRegion"};
        return regions ;
    }

    @Override
    protected void processReceivedEvent(Object msg) {
        System.out.println("Subscriber Got: {}" + msg.toString());
        if (msg instanceof IgnoreErroedEvent) {
            System.out.println("Worker Supervisor: Got and forwarding to the persistent actor worker: " + ((AssignmentEvent) msg).getEventName());
            defEventStoreSupervisorShardRegion.tell(msg, getSelf());
            caller = getSender();
        } else if (msg instanceof EDFEventDeliveryAck) {
            caller.tell(msg, getSelf());
        } else if (msg instanceof AssignmentEvent) {
            System.out.println("DefEventListener Got: {}" + ((AssignmentEvent) msg).getEventName());
            caller = getSender();
            Future<Object> cbFuture = getCircuitBreaker().callWithCircuitBreaker(new Callable<Future<Object>>() {
                @Override
                public Future<Object> call() throws Exception {
                    return Patterns.ask(defEventStoreSupervisorShardRegion, msg, Timeout.apply(responseTimeout, TimeUnit.MILLISECONDS));
                }
            });
            Patterns.pipe(cbFuture, getContext().system().dispatcher()).to(getSelf());
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


    @Override
    protected String actorName() {
        return "DefEventListener";
    }

}
