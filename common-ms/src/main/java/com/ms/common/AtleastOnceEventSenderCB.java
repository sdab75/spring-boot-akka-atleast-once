package com.ms.common;

import akka.actor.ActorRef;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.ms.event.EDFEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
public abstract class AtleastOnceEventSenderCB extends NonPersistentActor {
    private LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);


    public AtleastOnceEventSenderCB() {
        super();
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }

    private ActorRef caller;

    @Override
    protected void processReceivedEvent(Object msg) {
        if (msg instanceof EDFEvent) {
            ActorRef eventSender = getSender();
            Future<Object> cbFuture = getCircuitBreaker().callWithCircuitBreaker(new Callable<Future<Object>>() {
                @Override
                public Future<Object> call() throws Exception {
                    log("Sending msg to " + destinationPath() + " from ===>"+getSender().path() +"===>"+msg);
                    return Patterns.ask(destinationPath(), msg,getResponseTime());
                }
            });

            Patterns.pipe(cbFuture, getContext().system().dispatcher()).to(eventSender);
        }else {
            unhandled(msg);
        }
    }

    protected void log(String msg) {
        LOG.info(actorName() + " : " + msg);
    }

    protected abstract String actorName();

    protected abstract ActorRef destinationPath();

    protected abstract Timeout getResponseTime();

}