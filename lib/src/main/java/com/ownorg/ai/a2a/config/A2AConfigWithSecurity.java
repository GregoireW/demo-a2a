package com.ownorg.ai.a2a.config;

import com.ownorg.ai.a2a.config.user.A2ADefaultUserProvider;
import com.ownorg.ai.a2a.config.user.A2AUserProvider;
import com.ownorg.ai.a2a.controllers.AgentController;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;

import java.util.concurrent.Executor;

@AutoConfiguration
@ConditionalOnMissingBean(AgentController.class)
public class A2AConfigWithSecurity {

    @Bean("a2aTaskExecutor")
    @ConditionalOnMissingBean(name = "a2aTaskExecutor")
    public Executor a2aTaskExecutor() {

        return new DelegatingSecurityContextExecutor(new VirtualThreadTaskExecutor("a2a-server-task-"));
    }


    @ConditionalOnClass(name = "org.springframework.security.config.annotation.web.configuration.EnableWebSecurity")
    @Bean
    public A2AUserProvider a2aUserProvider() {
        return new A2ADefaultUserProvider();
    }
}
