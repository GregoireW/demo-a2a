package com.ownorg.ai.a2a.config;

import com.ownorg.ai.a2a.config.properties.AgentProperties;
import com.ownorg.ai.a2a.config.user.A2AUserProvider;
import com.ownorg.ai.a2a.controllers.AgentController;
import com.ownorg.ai.a2a.definition.Agent;
import com.ownorg.ai.a2a.definition.AgentDefinition;
import com.ownorg.ai.a2a.internal.AgentStore;
import com.ownorg.ai.a2a.internal.CdiAgentCardInstance;
import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.config.DefaultValuesConfigProvider;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.events.QueueManager;
import io.a2a.server.requesthandlers.DefaultRequestHandler;
import io.a2a.server.tasks.*;
import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@AutoConfiguration
@ConditionalOnMissingBean(AgentController.class)
@EnableConfigurationProperties(AgentProperties.class)
public class A2AConfig {

    @Bean
    @ConditionalOnMissingBean
    public TaskStore taskStore() {
        return new InMemoryTaskStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueueManager queueManager(TaskStore taskStore) {
        // InMemoryTaskStore implements both TaskStore and TaskStateProvider
        return new InMemoryQueueManager((TaskStateProvider) taskStore);
    }

    @Bean
    @ConditionalOnMissingBean
    public PushNotificationConfigStore pushNotificationConfigStore() {
        return new InMemoryPushNotificationConfigStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public A2AConfigProvider a2aConfigProvider() {
        return new DefaultValuesConfigProvider();
    }

    @ConditionalOnMissingClass("org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    @Configuration
    static class InternalA2AConfigNoSecurity extends A2AConfigNoSecurity {
    }

    @ConditionalOnClass(name = "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    @Configuration
    static class InternalA2AConfigWithSecurity extends A2AConfigWithSecurity {
    }


    @Bean
    @ConditionalOnMissingBean
    public AgentStore jsonRpcHandler(
            ApplicationContext applicationContext,
            AgentProperties agentProperties,
            @Qualifier("a2aTaskExecutor") Executor executor,
            TaskStore taskStore,
            QueueManager queueManager,
            PushNotificationConfigStore pushNotificationConfigStore) {

        Map<String, JSONRPCHandler> agentsMap=new HashMap<>();
        var agents=applicationContext.getBeansWithAnnotation(Agent.class);

        JSONRPCHandler wellKnownAgent=null;

        for (var agentEntry : agents.entrySet()) {
            var agentDef=agentEntry.getValue();
            if (!(agentDef instanceof AgentDefinition agent)) {
                throw new IllegalArgumentException("Agents must implement AgentDefinition interface. Agent "+ agentEntry.getKey() +" (" + agentDef.getClass().getName()+") is not.");
            }
            var agentCard = updateCard(agentEntry.getKey(), agent.publicCard(), agentProperties, agent.extendedCard()!=null);
            var extendedCard = updateCard(agentEntry.getKey(), agent.extendedCard(), agentProperties, true);
            var extendedCardParameters = new CdiAgentCardInstance(extendedCard);
            var requestHandler = DefaultRequestHandler.create(agent, taskStore, queueManager, pushNotificationConfigStore, null, executor);
            var jsonRpcHandler = new JSONRPCHandler(agentCard, extendedCardParameters, requestHandler, executor);
            agentsMap.put(agentEntry.getKey(), jsonRpcHandler);
            if (agentEntry.getKey().equals(agentProperties.wellKnownAgentName())) {
                wellKnownAgent = jsonRpcHandler;
            }
        }

        return new AgentStore(agentsMap, wellKnownAgent);
    }

    private AgentCard updateCard(String agentName, AgentCard.Builder builder, AgentProperties agentProperties, boolean hasExtendedCard) {
        if (builder==null) return null;
        var isWellKnownAgent = agentName.equals(agentProperties.wellKnownAgentName());
        var uriBuilder=UriComponentsBuilder.fromUri(agentProperties.baseUrl());
        if (isWellKnownAgent) {
            uriBuilder.path("agent");
        }else{
            uriBuilder.path("agents/" + agentName);
        }
        return builder.url(uriBuilder.toUriString())
                .preferredTransport(A2ATransportMetadata.TRANSPORT_PROTOCOL.asString())
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(true)
                        .stateTransitionHistory(true)
                        .build())
                .protocolVersion(null) // To force the use of the default protocol version
                .supportsAuthenticatedExtendedCard(hasExtendedCard)
                .build()
        ;
    }

    @Bean
    public AgentController agentController(AgentStore agentStore, A2AUserProvider a2aUserProvider) {
        return new AgentController(agentStore, a2aUserProvider);
    }
}
