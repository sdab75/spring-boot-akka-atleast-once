package com.ms.common;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.japi.Function;
import com.ms.abc.service.AsyncWrapperException;
import com.ms.event.IgnoreErroedEvent;
import com.ms.event.StoredEvent;
import scala.concurrent.duration.Duration;

import java.util.logging.Logger;

import static akka.actor.SupervisorStrategy.*;

/**
 * Created by davenkat on 12/17/2015.
 */
public class SuperVisorStrategyUtil {
    private static final Logger LOG = Logger.getLogger(String.valueOf(SuperVisorStrategyUtil.class));

    public static SupervisorStrategy listenerSupervisorStrategy(String actorName, ActorRef senderActor, ActorRef selfActor) {
        SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, Directive>() {
            @Override
            public Directive apply(Throwable th) {
                if (th instanceof AsyncWrapperException) {
                    Throwable t = th.getCause();
                    if (t instanceof UnRecoverableServiceException) {
                        log(actorName,"Raised an unrecoverable exception, escalated further !!!");
                        return escalate();
                    } else if (t instanceof RecoverableServiceException) {
                        log(actorName,"Raised an recoverable exception, actor will restarted");
                        return restart();
                    }else {
                        senderActor.tell(new IgnoreErroedEvent((StoredEvent) ((AsyncWrapperException) th).getObj()), selfActor);
                        log(actorName,"Raised an application exception, supervisor sent an Ignore Error Event to send an ack to the sender and resume the work ");
                        return resume();
                    }
                } else {
                    log(actorName,"Raised an unknown error, actor will ignore this message and resume work for next message");
                    return resume();
                }
            }
        });
        return strategy;
    }

    public static SupervisorStrategy persistentActorSupervisorStrategy(String actorName, ActorRef senderActor, ActorRef selfActor) {
        SupervisorStrategy strategy = new OneForOneStrategy(-1, Duration.create("5 seconds"), new Function<Throwable, Directive>() {
            @Override
            public Directive apply(Throwable th) {
                if (th instanceof AsyncWrapperException) {
                    Throwable t = th.getCause();
                    if (t instanceof UnRecoverableServiceException) {
                        log(actorName,"Raised an unrecoverable exception, escalated further !!!");
                        return escalate();
                    } else if (t instanceof RecoverableServiceException) {
                        log(actorName,"Raised an recoverable exception, actor will escalate the error to the parent");
                        return escalate();
                    }else {
                        senderActor.tell(new IgnoreErroedEvent((StoredEvent) ((AsyncWrapperException) th).getObj()), selfActor);
                        log(actorName,"Raised an application exception, supervisor sent an Ignore Error Event to send an ack to the sender and resume the work ");
                        return resume();
                    }
                } else {
                    log(actorName,"Raised an unknown error, actor will ignore this message and resume work for next message");
                    return resume();
                }
            }
        });
        return strategy;
    }

    private static  void log(String actorName,String msg){
        LOG.info(actorName+" : "+msg);
    }

}
