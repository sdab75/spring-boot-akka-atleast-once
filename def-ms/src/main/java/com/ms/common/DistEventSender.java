package com.ms.common;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.UntypedActor;
import akka.cluster.client.ClusterClientReceptionist;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import com.ms.event.EDFEvent;
import com.ms.event.EDFEventDeliveryAck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

/**
 * Created by davenkat on 9/28/2015.
 */
@Component
public abstract class DistEventSender extends NonPersistentActor {
    private static final Logger log = LoggerFactory.getLogger(DistEventSender.class);

    // activate the extension
    ActorRef mediator =DistributedPubSub.get(getContext().system()).mediator();
    public DistEventSender(){
        mediator.tell(new DistributedPubSubMediator.Put(getSelf()), getSelf());
        ClusterClientReceptionist.get(getContext().system()).registerService(getSelf());
    }
    private ActorRef caller;

    @Override
    protected void processReceivedEvent(Object msg) {
        if (msg instanceof EDFEvent) {
            System.out.println("Def DistEventSender: Received message from ===>" + getSender().path() + "===>" + msg.toString());
            caller = getSender();

            Future<Object> cbFuture = circuitBreaker.callWithCircuitBreaker(new Callable<Future<Object>>() {
                @Override
                public Future<Object> call() throws Exception {
                    return Patterns.ask(mediator, new DistributedPubSubMediator.Send(destinationPath(), msg, false), ASK_TIMEOUT);
                }
            });
            Patterns.pipe(cbFuture, getContext().system().dispatcher()).to(caller);

            //   mediator.tell(new DistributedPubSubMediator.Send(destinationPath(), msg, false), getSelf());
        }else if (msg instanceof EDFEventDeliveryAck) {
//            String path="/user/eventSender";
            ActorSelection eventSenderRef = getContext().actorSelection(senderAckPath());
            System.out.println("Def DistEventSender Sending confirmation back to ===>" + eventSenderRef.path() + "===>" + msg.toString());
            /*Step4: The destination (MyDestination) sends a Confirm object to the sender (EventSender). */
            EDFEventDeliveryAck confirm = (EDFEventDeliveryAck) msg;
            eventSenderRef.tell(confirm, getSelf());
        }
        else if (msg instanceof DistributedPubSubMediator.SubscribeAck)
            System.out.println("publisher subscribing");
        else {
            unhandled(msg);
        }

    }
    protected abstract String destinationPath();
    protected abstract String senderAckPath();

}