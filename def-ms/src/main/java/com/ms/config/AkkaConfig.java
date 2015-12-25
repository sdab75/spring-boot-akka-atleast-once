package com.ms.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.routing.RoundRobinPool;
import com.ms.event.AssignmentEvent;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Optional;

@Configuration
//@Lazy
/*

@ComponentScan(basePackages = {"com.cgi.garnet.attachment.config",
        "com.cgi.garnet.attachment.rest", "com.cgi.garnet.attachment.service"})
*/
public class AkkaConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaConfig.class);


    @Autowired
    private SpringExtension springExtension;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Actor system singleton for this application.
     */
    @Bean
    public ActorSystem actorSystem() {
        ActorSystem actorSystem = ActorSystem.create("ClusterSystem", akkaConfiguration());
        springExtension.initialize(applicationContext);
        return actorSystem;
    }

    @Bean
    public ActorRef defToAbcEventSenderActor() {
        return actorSystem().actorOf(springExtension.props("defToAbcEventSender").withRouter(new RoundRobinPool(1)), "defToAbcEventSender");
    }

    @Bean
    public ActorRef abcToDefDistEventSenderActor() {
        return actorSystem().actorOf(springExtension.props("defToAbcDistEventSender").withRouter(new RoundRobinPool(1)), "defToAbcDistEventSender");
    }

    @Bean
    public ClusterShardingSettings initClusterShardingSettings(){
        return ClusterShardingSettings.create(actorSystem()).withRole("defService");
    }

    @Bean
    public ClusterSharding clusterSharding(){
        return  ClusterSharding.get(actorSystem());
    }

    @Bean
    public Props defEventStoreSupervisorProps(){
        return springExtension.props("defEventStoreSupervisor");
    }

    @Bean
    public Props defEventListenerProps(){
        return springExtension.props("defEventListener");
    }

    @Bean
    public Props defEventStoreActorProps(){
        return springExtension.props("defEventStoreActor");
    }


    /**
     * Always start the top supervisor. Let the supervisor create it's own children in this case listerSuperVisor has Listener Actor and listener Actor it self is a supervisor for worker.
     * @return
     */

    @Bean
    public ActorRef initdefEventStoreSupervisor() {
        ActorRef sub = actorSystem().actorOf(defEventStoreSupervisorProps(), "defEventStoreSupervisor");
        return sub;

    }

    @Bean
    public ActorRef initdefEventListener() {
        ActorRef sub = actorSystem().actorOf(defEventListenerProps(), "defEventListener");
        return sub;

    }


    @Bean
    public ActorRef defEventStoreActorShardRegion() {
        return clusterSharding().start("defEventStoreActor", defEventStoreActorProps(), initClusterShardingSettings(), defShardignessageExtractor());
    }

    @Bean
    public ActorRef defEventStoreSupervisorShardRegion() {
        return clusterSharding().start("defEventStoreSupervisor", defEventStoreSupervisorProps(), initClusterShardingSettings(), defShardignessageExtractor());
    }

    @Bean
    public ActorRef abcEventStoreActorShardRegion() {
        return clusterSharding().startProxy("abcEventStoreActor",Optional.of("abcService") , defShardignessageExtractor());
    }

    @Bean
    public ActorRef abcEventStoreSupervisorShardRegion() {
        return clusterSharding().startProxy("abcEventStoreSupervisor", Optional.of("abcService"), defShardignessageExtractor());
    }

    @Bean
    @Scope(value = "prototype")
    public ShardRegion.MessageExtractor defShardignessageExtractor() {
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
                    String shardId=String.valueOf(uid.length() % numberOfShards);;
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

    /**
     * Read configuration from application.conf file
     */
    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

}
