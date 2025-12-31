package com.ownorg.ai.demo_a2a.config;

import io.a2a.client.Client;
import io.a2a.client.http.A2ACardResolver;
import io.a2a.client.http.A2AHttpClient;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.AgentCard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutor;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
public class A2AConfig {

    @Bean
    public Client agentClient(RestClient restClient) {
        AgentCard publicAgentCard = new A2ACardResolver("http://localhost:8080").getAgentCard();

        var executor=new DelegatingSecurityContextExecutor(new VirtualThreadTaskExecutor("a2a-client-task-"));

        A2AHttpClient httpClient = new RestClientA2AHttp(restClient, executor);

        return Client
                .builder(publicAgentCard)
                .addConsumers(List.of())
                .streamingErrorHandler(t->{})
                .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig(httpClient))
                .build();
    }

}
