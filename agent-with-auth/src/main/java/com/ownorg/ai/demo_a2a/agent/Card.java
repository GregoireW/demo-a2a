package com.ownorg.ai.demo_a2a.agent;

import io.a2a.spec.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
public class Card {
    @Bean
    public AgentCard agentCard() {
        // NOTE: Transport validation will automatically check that transports specified
        // in this AgentCard match those available on the classpath when handlers are initialized

        var securityScheme = new OpenIdConnectSecurityScheme.Builder().openIdConnectUrl("http://localhost:8080/.well-known/openid-configuration").build();

        return new AgentCard.Builder()
                .name("Hello World Agent")
                .description("Just a hello world agent")
                .url("http://localhost:8080/agent")
                .version("1.0.0")
                .documentationUrl("http://example.com/docs")
                .capabilities(new AgentCapabilities.Builder()
                        .streaming(true)
                        .pushNotifications(true)
                        .stateTransitionHistory(true)
                        .build())
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(Collections.singletonList(new AgentSkill.Builder()
                        .id("hello_world")
                        .name("Returns hello world")
                        .description("just returns hello world")
                        .tags(Collections.singletonList("hello world"))
                        .examples(List.of("hi", "hello world"))
                        .build()))
                .security(List.of(Map.of(OpenIdConnectSecurityScheme.OPENID_CONNECT,
                                List.of("oidc", "profile"))))
                .securitySchemes(Map.of(OpenIdConnectSecurityScheme.OPENID_CONNECT, securityScheme))
                .build();
    }
}
