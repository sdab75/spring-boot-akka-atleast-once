package com.ms.common;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import com.ms.abc.service.AppException;
import com.ms.event.EDFEvent;
import com.ms.event.EDFEventDeliveryAck;
import com.ms.event.IgnoreErroedEvent;
import com.ms.event.StoredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * Created by davenkat on 9/28/2015.
 */
@Component
public abstract class PersistentActor extends UntypedPersistentActor {
    private static final Logger LOG = LoggerFactory.getLogger(PersistentActor.class);

    @Override
    public void preStart() throws Exception {
        System.out.println(getCmdProcessorName() + " Starting ......");
        super.preStart();
    }

    @Override
    public String persistenceId() {
        return getCmdProcessorName() + "-" + getContext().parent().path().name();
    }


    @Override
    public boolean recoveryFinished() {
        System.out.println(getCmdProcessorName() + " Recovery Finished !!!");
        return super.recoveryFinished();
    }

    @Override
    public void onRecoveryFailure(Throwable cause, scala.Option<Object> event) {
        LOG.error(getCmdProcessorName() + " Recovery Failed !!!");
        super.onRecoveryFailure(cause, event);
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof StoredEvent) {
            StoredEvent storedEvent = (StoredEvent) msg;
            System.out.println(getCmdProcessorName() + " Recovered Event -->" + storedEvent.getEDFEvent().toString());
            processStoredEvent(storedEvent);
        } else {
            unhandled(msg);
        }
    }

    @Override
    public void onReceiveCommand(Object cmd) {
        try {
            //Arming the security
            processCommand(cmd);
        } finally {
        }

    }

    public static int count = 0;

    /**
     * Common orchestration for a persistent actor.
     *
     * @param cmd
     */
    protected void processCommand(Object cmd) {
        if (cmd instanceof IgnoreErroedEvent) {
            System.out.println(" Persistent actor received IgnoreErroedEvent from the supervisor...");
            //This is a persisted event and failed for some reason. The supervisor determines received error can't be processed further and can't retry anymore.
            //To achieve this
            saveSnapshot(((IgnoreErroedEvent) cmd).getStoredEvent());
        } else if (cmd instanceof EDFEvent) {
            EDFEvent evt = ((EDFEvent) cmd);
            System.out.println(getCmdProcessorName() + " Received command ...");
            if (validateEvent(evt)) {
                StoredEvent storedEvent = new StoredEvent(evt);
                persist(storedEvent, new Procedure<StoredEvent>() {
                    public void apply(StoredEvent storedEvent) throws Exception {
                        processStoredEvent(storedEvent);
                    }
                });
                sleep();
                getSender().tell(new EDFEventDeliveryAck(evt.getEventDeliveryId(), true), getSelf());
            } else {
                sleep();
                getSender().tell(new EDFEventDeliveryAck(evt.getEventDeliveryId(), false), getSelf());
            }
        } else if (cmd instanceof DistributedPubSubMediator.SubscribeAck) {
            System.out.println(getCmdProcessorName() + " successfully subscribed for receiving event messages !!!!");
        } else if (cmd.equals(ReceiveTimeout.getInstance())) {
            System.out.println(getCmdProcessorName() + " received idle time out kicked off and restart the Actor !!!!");
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else
            unhandled(cmd);
    }

    private void sleep() {
/*
        try {
            if (count ==0) {
                System.out.println("Counter " + count + "==> waiting for ==>" + 4);
                count++;
                Thread.sleep(10000);
            }else{
                count = 0;
                System.out.println("Not sleeping...");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
*/

    }

    /**
     * Executes the stored Event
     *
     * @param storedEvent
     */
    private void processStoredEvent(StoredEvent storedEvent) {
        {
            try {
                //Arming the security
                System.out.println(getCmdProcessorName() + " stored EDFEvent successfully, processing of the stored Event started ..");
                try {
                    preProcessEvent(storedEvent.getEDFEvent());
                    processEvent(storedEvent.getEDFEvent());
                    saveSnapshot(storedEvent);
                    postProcessEvent(storedEvent.getEDFEvent());
                } catch (Exception e) {
                    throw new WrapperException("Exception raised while persistent actor processing the event !!  ", storedEvent, e);
                }
                System.out.println(getCmdProcessorName() + " EDF Event processing finished ...");

                //For any further event publishing
                EDFEvent eventToPublish = publishDoneEvent(storedEvent.getEDFEvent());
                if (eventToPublish != null) {
                    //The child class determined to publish the done event.
                    getContext().system().eventStream().publish(eventToPublish);
                }

            } finally {
            }
        }
    }

    abstract protected String getCmdProcessorName();

    abstract protected boolean validateEvent(EDFEvent edfEvent);

    abstract protected void preProcessEvent(EDFEvent edfEvent);

    abstract protected void processEvent(EDFEvent edfEvent);

    abstract protected void postProcessEvent(EDFEvent edfEvent);

    abstract protected EDFEvent publishDoneEvent(EDFEvent edfEvent);
}
