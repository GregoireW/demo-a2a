package com.ownorg.ai.a2a.config;

import com.ownorg.ai.a2a.config.user.A2AAnonymousUserProvider;
import com.ownorg.ai.a2a.config.user.A2AUserProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.VirtualThreadTaskExecutor;

import java.util.concurrent.Executor;

public class A2AConfigNoSecurity {

    @Bean("a2aTaskExecutor")
    @ConditionalOnMissingBean(name = "a2aTaskExecutor")
    public Executor a2aTaskExecutor() {

        return new VirtualThreadTaskExecutor("a2a-server-task-");
    }

    @Bean
    public A2AUserProvider a2aAnonymousUserProvider() {
        return new A2AAnonymousUserProvider();
    }
}
