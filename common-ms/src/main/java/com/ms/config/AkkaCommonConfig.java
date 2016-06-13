package com.ms.config;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.cluster.Cluster;
import akka.cluster.sharding.ClusterSharding;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class AkkaCommonConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(AkkaCommonConfig.class);
    private @Value(value = "${akka.cluster.member-nodes}") String nodes;
//    String nodes="127.0.0.1:2550,127.0.0.1:2551,127.0.0.1:2552";

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
        final Cluster cluster = Cluster.get(actorSystem);

        List<Address> memberList = new ArrayList<Address>();
        int count = cluster.state().members().size();
        if(count==0){
            LOG.info("********* CLUSTER has not formed this node will start the cluster and become the first seed node. ***********");
        }else{
            LOG.info("********* Found an existing cluster will join as member to the cluster. **********");
        }
        memberList.add(Cluster.get(actorSystem).selfAddress());
        if (StringUtils.isNotBlank(nodes)) {
            String[] nodeList = nodes.split(",");
            String hostPort[]=null;
            for (String node:nodeList) {
                hostPort = node.split(":");
                memberList.add(new Address("akka.tcp", "ClusterSystem", hostPort[0],
                        Integer.parseInt(hostPort[1])));
            }
        }

        Cluster.get(actorSystem).joinSeedNodes(memberList);

        return actorSystem;

    }

    @Bean
    public ClusterSharding clusterSharding(){
        return  ClusterSharding.get(actorSystem());
    }

    @Bean
    public ActorRef initClusterMemberManagementActor() {
        ActorRef sub = actorSystem().actorOf(springExtension.props("clusterMemberManagementActor"), "clusterMemberManagementActor");
        return sub;
    }
    @Bean
    public ActorRef deadLetterInit() {
        ActorRef actor=actorSystem().actorOf(springExtension.props("deadLetterActor"), "deadLetterActor");
        return actor;
    }

    /**
     * Read configuration from application.conf file
     */
    @Bean
    public Config akkaConfiguration() {
        return ConfigFactory.load();
    }

}
