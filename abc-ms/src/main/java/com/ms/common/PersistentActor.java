package com.ms.common;

import akka.actor.PoisonPill;
import akka.actor.ReceiveTimeout;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.sharding.ShardRegion;
import akka.japi.Procedure;
import akka.persistence.UntypedPersistentActor;
import com.ms.event.EDFEvent;
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
        System.out.println("PersistentActor Startup ###########################");
        super.preStart();
    }

    @Override
    public boolean recoveryFinished() {
        LOG.info(" PersistentActor Recovery Finished !!!");
        return super.recoveryFinished();
    }

    @Override
    public void onRecoveryFailure(Throwable cause, scala.Option<Object> event) {
        LOG.error(" PersistentActor Recovery Failed !!!");
        super.onRecoveryFailure(cause, event);
    }


    @Override
    public void onPersistFailure(Throwable cause, Object event, long seqNr) {
        LOG.info(" Persistenced failed and the actor will be stopped!!!");
        super.onPersistFailure(cause, event, seqNr);
    }

    @Override
    public void onReceiveRecover(Object msg) {
        if (msg instanceof StoredEvent) {
            StoredEvent storedEvent = (StoredEvent) msg;
            LOG.info(" Recovered Event -->" + storedEvent.getEDFEvent().toString());
            processRecoveredEvent(storedEvent);
        } else {
            unhandled(msg);
        }
    }

    @Override
    public void onReceiveCommand(Object cmd) {
        processCommand(cmd);
    }

    /**
     * Process the recovered Event.
     *
     * @param storedEvent
     */
    protected void processRecoveredEvent(StoredEvent storedEvent) {
        processEvent(storedEvent.getEDFEvent());
    }

    /**
     * Common orchestration for a persistent actor.
     *
     * @param cmd
     */
    protected void processCommand(Object cmd) {
        if (cmd instanceof EDFEvent) {
            EDFEvent evt = ((EDFEvent) cmd);
            if (validateEvent(evt)) {
                StoredEvent storedEvent = new StoredEvent(evt);
                persistAsync(storedEvent, new Procedure<StoredEvent>() {
                    public void apply(StoredEvent storedEvent) throws Exception {

                        LOG.info(" Validated EDFEvent stored successfully, processing the stored Event -->" + storedEvent.getEDFEvent().toString());
                        processEvent(storedEvent.getEDFEvent());
                        LOG.info(" EDF Event processing finished -->" + storedEvent.getEDFEvent().toString());

                        //For any further event publishing
                        publishDoneEvent(storedEvent.getEDFEvent());
                        saveSnapshot(storedEvent);
                    }
                });
            }
        } else if (cmd instanceof DistributedPubSubMediator.SubscribeAck) {
            LOG.info("PersistentActor successfully subscribed for receiving event messages !!!!");
        } else if (cmd.equals(ReceiveTimeout.getInstance())) {
            LOG.info("PersistentActor received idle time out kicked off and restart the Actor !!!!");
            getContext().parent().tell(new ShardRegion.Passivate(PoisonPill.getInstance()), getSelf());
        } else
            unhandled(cmd);
    }

    abstract protected boolean validateEvent(EDFEvent edfEvent);

    abstract protected void processEvent(EDFEvent edfEvent);

    abstract protected void publishDoneEvent(EDFEvent edfEvent);
}
