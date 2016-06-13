package com.ms.common;

import akka.actor.ActorContext;
import akka.actor.ActorRef;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ShardRegion;
import org.apache.commons.lang.StringUtils;

/**
 * Created by davenkat on 6/4/2016.
 */
public class ClusterMemberManagementUtil {

    public static void log(String actorName, String msg) {
        System.out.println(actorName + " : " + msg);
    }

    public static boolean oneMemberLeft(Cluster cluster, String actorName) {
        Iterable<Member> memberIter = cluster.state().getMembers();

        int clusterMemebersCount = cluster.state().members().size();
        int totalMemberUpCount = 0;
        log(actorName, " Remaining Cluster Members Count =====> " + clusterMemebersCount);
        for (Member m : memberIter) {
            log(actorName, " Remaining Cluster Members =====> " + m.toString());
            if (m.status().equals(MemberStatus.up())) {
                totalMemberUpCount++;
            }
        }
        if (totalMemberUpCount == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static void watchShardRegion(ActorContext context, ActorRef self, String actorName, String... regionNames) {
        {
            if (regionNames != null) {
                for (String regionName : regionNames) {
                    if (StringUtils.isNotEmpty(regionName)) {
                        log(actorName,"ClusterMemberManagementUtil Started watching Region Name=======================>" + regionName);

                        final ActorRef region = ClusterSharding.get(context.system()).shardRegion(regionName);
                        context.watch(region);

                    }
                }
            }

        }

    }

    public static void cleanShardingCoordinators(Object msg, ActorContext context, ActorRef self, String actorName, String... regionNames) {

        Cluster cluster=Cluster.get(context.system());
        if (msg instanceof ClusterEvent.MemberRemoved
                && ClusterMemberManagementUtil.oneMemberLeft(cluster,null)) {
            log(actorName,"ClusterMemberManagementUtil Member Removed =======================>" + msg.toString());
            if (regionNames != null) {
                for (String regionName : regionNames) {
                    if (StringUtils.isNotEmpty(regionName)) {
                        log(actorName,"ClusterMemberManagementUtil Removing Region Name=======================>" + regionName);

                        final ActorRef region = ClusterSharding.get(context.system()).shardRegion(regionName);
                        region.tell(ShardRegion.gracefulShutdownInstance(), self);

                    }
                }
            }
        }
    }
}
