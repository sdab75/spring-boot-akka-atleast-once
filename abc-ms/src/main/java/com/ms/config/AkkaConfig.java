package com.ms.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import com.ms.event.AssignmentEvent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
//@Lazy
/*
@Lazy
@ComponentScan(basePackages = {"com.cgi.garnet.attachment.config",
        "com.cgi.garnet.attachment.rest", "com.cgi.garnet.attachment.service"})
*/
public class AkkaConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaConfig.class);

    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private ActorSystem actorSystem;

    @Autowired
    private ClusterSharding clusterSharding;
    @Bean
    public ClusterShardingSettings initClusterShardingSettings(){
        return ClusterShardingSettings.create(actorSystem).withRole("abcService");
    }

    @Bean
    public Props abcEventStoreSupervisorProps(){
        return springExtension.props("abcEventStoreSupervisor");
    }

    @Bean
    public Props abcEventListenerProps(){
        return springExtension.props("abcEventListener");
    }

    @Bean
    public Props abcEventStoreActorProps(){
        return springExtension.props("abcEventStoreActor");
    }


    /**
     * Always start the top supervisor. Let the supervisor create it's own children in this case listerSuperVisor has Listener Actor and listener Actor it self is a supervisor for worker.
     * @return
     */

    @Bean
    public ActorRef initAbcEventStoreSupervisor() {
        ActorRef sub = actorSystem.actorOf(abcEventStoreSupervisorProps(), "abcEventStoreSupervisor");
        return sub;

    }

    @Bean
    public ActorRef initAbcEventListener() {
        ActorRef sub = actorSystem.actorOf(abcEventListenerProps(), "abcEventListener");
        return sub;

    }

    /*
    Shard Region Definitions
     */

    @Bean
    public ActorRef abcToDefEventSenderShardRegion() {
        return clusterSharding.start("abcToDefEventSenderShardRegion", springExtension.props("abcToDefEventSender"), initClusterShardingSettings(), abcShardignessageExtractor());
    }

    @Bean
    public ActorRef defListenerShardRegionProxy() {
        //starx proxy name has to match the exact shard region name of the target actor.
        return clusterSharding.startProxy("defListenerShardRegion",Optional.of("defService") , abcShardignessageExtractor());
    }


    @Bean
    public ActorRef abcEventStoreSupervisorShardRegion() {
        return clusterSharding.start("abcEventStoreSupervisor", abcEventStoreSupervisorProps(), initClusterShardingSettings(), abcShardignessageExtractor());
    }

    @Bean
    public ActorRef abcListenerShardRegion() {
        return clusterSharding.start("abcListenerShardRegion", springExtension.props("abcEventListener"), initClusterShardingSettings(), abcShardignessageExtractor());
    }




    @Bean
    @Scope(value = "prototype")
    public ShardRegion.MessageExtractor abcShardignessageExtractor() {
        ShardRegion.MessageExtractor  messageExtractor = new ShardRegion.MessageExtractor() {
            @Override
            public Object entityMessage(Object message) {
                return message;
            }

            @Override
            public String entityId(Object message) {
                if (message instanceof AssignmentEvent) {
                    String id=((AssignmentEvent) message).getModuleId().toString();
                    return id;
                }
                return  null;
            }
            @Override
            public String shardId(Object message) {
                int numberOfShards = 100;
                if (message instanceof AssignmentEvent) {
                    String uid = ((AssignmentEvent) message).getModuleId().toString();
                    String shardId=String.valueOf(uid.hashCode() % numberOfShards);;
                    System.out.println("ShardId --->" + shardId);
                    return shardId;
                } else {
                    System.out.println("ShardId is null ??????????????????????");
                    return null;
                }
            }

        };
        return messageExtractor;
    }
}
