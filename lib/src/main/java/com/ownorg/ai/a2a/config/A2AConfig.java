package com.ownorg.ai.a2a.config;

import com.ownorg.ai.a2a.config.user.A2AUserProvider;
import com.ownorg.ai.a2a.controllers.AgentController;
import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.config.A2AConfigProvider;
import io.a2a.server.config.DefaultValuesConfigProvider;
import io.a2a.server.events.InMemoryQueueManager;
import io.a2a.server.events.QueueManager;
import io.a2a.server.requesthandlers.DefaultRequestHandler;
import io.a2a.server.tasks.*;
import io.a2a.spec.AgentCard;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@AutoConfiguration
@ConditionalOnMissingBean(AgentController.class)
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
    public JSONRPCHandler jsonRpcHandler(AgentCard agentCard,
                                            @Qualifier("a2aTaskExecutor") Executor executor,
                                            AgentExecutor agentExecutor,
                                            TaskStore taskStore,
                                            QueueManager queueManager,
                                            PushNotificationConfigStore pushNotificationConfigStore) {
        var requestHandler = DefaultRequestHandler.create(agentExecutor, taskStore, queueManager, pushNotificationConfigStore, null, executor);
        return new JSONRPCHandler(agentCard, requestHandler, executor);
    }

    @Bean
    public AgentController agentController(JSONRPCHandler jsonRpcHandler, A2AUserProvider a2aUserProvider) {
        return new AgentController(jsonRpcHandler, a2aUserProvider);
    }
}
