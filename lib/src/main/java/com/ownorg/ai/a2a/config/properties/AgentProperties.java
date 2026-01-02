package com.ownorg.ai.a2a.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;

@ConfigurationProperties(prefix = "a2a.agent")
public record AgentProperties(URI baseUrl, String wellKnownAgentName) {
    public AgentProperties {
        if (baseUrl==null) baseUrl=URI.create("http://localhost:8080");
    }
}
