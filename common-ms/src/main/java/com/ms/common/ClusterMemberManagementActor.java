package com.ms.common;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by davenkat on 6/3/2016.
 */
@Component
@Scope("prototype")
public class ClusterMemberManagementActor extends UntypedActor {
    private static final Logger LOG = LoggerFactory.getLogger(ClusterMemberManagementActor.class);
    private Cluster cluster = Cluster.get(getContext().system());
    private ActorSystem system = getContext().system();
    private String actorName = "ClusterMemberManagementActor";
    List<ActorRef> shardRegions = new ArrayList<ActorRef>();

    @Override
    public void preStart() throws Exception {
        //#subscribe
        cluster.subscribe(
                getSelf(),
                ClusterEvent.initialStateAsEvents(),
                ClusterEvent.MemberEvent.class,
                ClusterEvent.UnreachableMember.class,
                ClusterEvent.MemberUp.class,
                ClusterEvent.MemberExited.class,
                ClusterEvent.MemberRemoved.class
        );
        super.preStart();
    }

    @Override
    public void postStop() throws Exception {
        //cluster.unsubscribe(getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ClusterEvent.MemberRemoved) {
            ClusterEvent.MemberRemoved mRemoved = (ClusterEvent.MemberRemoved) message;
            log(actorName, "Member Removed ==>" + mRemoved.member());
            if(ClusterMemberManagementUtil.oneMemberLeft(cluster,actorName)){
                cluster.leave(cluster.selfAddress());
                Cluster.get(system).registerOnMemberRemoved(new Runnable() {
                    @Override
                    public void run() {
                        int status = 0;
                        int maxDelayMillis = 10000;
                        try {
                            // exit JVM when ActorSystem has been terminated
                            final Runnable exit = new Runnable() {
                                @Override
                                public void run() {
                                    System.exit(0);
                                }
                            };
                            log(actorName, "*****************SHUTTING DOWN THE MEMBER********************");
                            system.registerOnTermination(exit);
                            // shut down ActorSystem
                            system.terminate();
                            // setup a timer, so if nice exit fails, the nasty exit happens
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    Runtime.getRuntime().halt(status);
                                }
                            }, maxDelayMillis);
                            // try to exit nicely
                            System.exit(status);

                        } catch (Throwable ex) {
                            // exit nastily if we have a problem
                            Runtime.getRuntime().halt(status);
                        } finally {
                            // should never get here
                            Runtime.getRuntime().halt(status);
                        }
                    }
                });
            }
        } else if (message instanceof Terminated) {
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            LOG.info("Member Terminated ==>", mUp.member());
        } else if (message instanceof ClusterEvent.MemberUp) {
            ClusterEvent.MemberUp mUp = (ClusterEvent.MemberUp) message;
            LOG.info("Member is Up ==>", mUp.member());
        } else if (message instanceof ClusterEvent.UnreachableMember) {
            ClusterEvent.UnreachableMember mUnreachable = (ClusterEvent.UnreachableMember) message;
            LOG.info("Member is Unreachable, going to leave the cluster ==>", mUnreachable.member());
            final Cluster cluster = Cluster.get(getContext().system());
            cluster.leave(mUnreachable.member().address());
            LOG.info("Member removing ==>", mUnreachable.member());
        }
    }

    public void log(String actorName, String msg) {
        System.out.println(actorName + " : " + msg);
    }

}
