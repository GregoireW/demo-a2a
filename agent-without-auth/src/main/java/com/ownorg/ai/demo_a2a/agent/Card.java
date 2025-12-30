package com.ownorg.ai.demo_a2a.agent;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

@Configuration
public class Card {
    @Bean
    public AgentCard agentCard() {
        // NOTE: Transport validation will automatically check that transports specified
        // in this AgentCard match those available on the classpath when handlers are initialized
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
                //use the default protocol version from sdk version .protocolVersion("0.3.0")
                .build();
    }
}
