package com.ms.common;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.ms.event.EDFEvent;
import com.ms.event.EDFEventDeliveryAck;
import org.springframework.stereotype.Component;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
public abstract class DistEventSender extends NonPersistentActor {
    private LoggingAdapter LOG = Logging.getLogger(getContext().system(), this);

    // activate the extension
    ActorRef mediator =DistributedPubSub.get(getContext().system()).mediator();
    public DistEventSender(){

        super();
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }
    private ActorRef caller;

    @Override
    protected void processReceivedEvent(Object msg) {
        if (msg instanceof EDFEvent) {
            log("Received msg from ===>"+getSender().path() +"===>"+msg.toString());
            caller = getSender();
            Future<Object> cbFuture = getCircuitBreaker().callWithCircuitBreaker(new Callable<Future<Object>>() {
                @Override
                public Future<Object> call() throws Exception {
//                    return Patterns.ask(mediator, new DistributedPubSubMediator.Send(destinationPath().path().toString(), msg, false),getResponseTime());
                    return Patterns.ask(destinationPath(), msg,getResponseTime());
                }
            });
            Patterns.pipe(cbFuture, getContext().system().dispatcher()).to(caller);
        }else if (msg instanceof EDFEventDeliveryAck) {
            ActorSelection eventSenderRef = getContext().actorSelection(senderAckPath());
            log("Sending confirmation back to ===>" + eventSenderRef.path() + "===>" + msg.toString());
            /*Step4: The destination (MyDestination) sends a Confirm object to the sender (EventSender). */
            EDFEventDeliveryAck confirm = (EDFEventDeliveryAck) msg;
            eventSenderRef.tell(confirm, getSelf());
        }
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            log("publisher subscribing");
        else {
            unhandled(msg);
        }
    }
    protected void log(String msg){
        LOG.info(actorName()+" : "+msg);
    }

    protected abstract String actorName();
    protected abstract ActorRef destinationPath();
    protected abstract String senderAckPath();
    protected abstract Timeout getResponseTime();

}