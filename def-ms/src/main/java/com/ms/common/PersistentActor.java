package com.ms.common;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import com.ms.event.EDFEvent;
import com.ms.event.EDFEventDeliveryAck;
import com.ms.event.IgnoreErroedEvent;
import com.ms.event.StoredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public abstract class PersistentActor extends UntypedPersistentActor {
    private static final Logger LOG = LoggerFactory.getLogger(PersistentActor.class);


    @Override
    public void preStart() throws Exception {
        LOG.info(actorName() + " Starting ......");
        super.preStart();
    }

    @Override
    public String persistenceId() {
        return actorName() + "-" + getContext().parent().path().name();
    }


    @Override
    public boolean recoveryFinished() {
        LOG.info(actorName() + " Recovery Finished !!!");
        return super.recoveryFinished();
    }

    @Override
    public void onRecoveryFailure(Throwable cause, scala.Option<Object> event) {
        LOG.error(actorName() + " Recovery Failed !!!");
        super.onRecoveryFailure(cause, event);
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof StoredEvent) {
            StoredEvent storedEvent = (StoredEvent) msg;
            log("Recovered Event -->" + storedEvent.getEDFEvent().toString());
            processStoredEvent(storedEvent);
        } else {
            unhandled(msg);
        }
    }

    @Override
    public void onReceiveCommand(Object cmd) {
        try {
            processCommand(cmd);
        } finally {
        }

    }

    /**
     * Common orchestration for a persistent actor.
     *
     * @param cmd
     */
    protected void processCommand(Object cmd) {
        if (cmd instanceof IgnoreErroedEvent) {
            log(" Persistent actor received IgnoreErroedEvent from the supervisor...");
            //This is a persisted event and failed for some reason. The supervisor determines received error can't be processed further and can't retry anymore.
            //To achieve this
            saveSnapshot(((IgnoreErroedEvent) cmd).getStoredEvent());
        } else if (cmd instanceof EDFEvent) {
            EDFEvent evt = ((EDFEvent) cmd);
            log("Received command ...");
            if (validateEvent(evt)) {
                StoredEvent storedEvent = new StoredEvent(evt);
                persist(storedEvent, new Procedure<StoredEvent>() {
                    public void apply(StoredEvent storedEvent) throws Exception {
                        processStoredEvent(storedEvent);
                    }
                });
                getSender().tell(new EDFEventDeliveryAck(evt.getEventDeliveryId(), true), getSelf());
            } else {
                getSender().tell(new EDFEventDeliveryAck(evt.getEventDeliveryId(), false), getSelf());
            }
        } else if (cmd instanceof DistributedPubSubMediator.SubscribeAck) {
            log("successfully subscribed for receiving event messages !!!!");
        } else if (cmd.equals(ReceiveTimeout.getInstance())) {
            log("received idle time out kicked off and restart the Actor !!!!");
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else
            unhandled(cmd);
    }

    /**
     * Executes the stored Event
     *
     * @param storedEvent
     */
    private void processStoredEvent(StoredEvent storedEvent) {
        {

            try {
                try {
                    log("stored EDFEvent successfully, processing of the stored Event started ..");
                    preProcessEvent(storedEvent.getEDFEvent());
                    processEvent(storedEvent.getEDFEvent());
                    saveSnapshot(storedEvent);
                    postProcessEvent(storedEvent.getEDFEvent());
                    log("EDF Event processing finished ...");
                } catch (Exception e) {
                    throw new AsyncWrapperException("Exception raised while persistent actor processing the event !!  ", storedEvent, e);
                }

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

    protected void log(String msg) {
        LOG.info(actorName() + " : " + msg);
    }

    abstract protected String actorName();

    abstract protected boolean validateEvent(EDFEvent edfEvent);

    abstract protected void preProcessEvent(EDFEvent edfEvent);

    abstract protected void processEvent(EDFEvent edfEvent);

    abstract protected void postProcessEvent(EDFEvent edfEvent);

    abstract protected EDFEvent publishDoneEvent(EDFEvent edfEvent);
}
