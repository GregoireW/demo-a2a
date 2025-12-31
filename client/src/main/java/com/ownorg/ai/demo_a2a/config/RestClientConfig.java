package com.ownorg.ai.demo_a2a.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        // This process should use the OAuth2ClientHttpRequestInterceptor with token exchange mechanism & co, but let's keep it simple for now
        builder.requestInterceptor(authentCalculator());
        return builder.build();

    }


    private ClientHttpRequestInterceptor authentCalculator() {

        return (request, body, next) -> {

            if ("localhost".equals(request.getURI().getHost())) {
                // This should select the token exchange mechanism if the call come from an authenticated user, or client credentials otherwise
                if(SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                    var token=jwtAuth.getToken().getTokenValue();
                    request.getHeaders().add("Authorization", "Bearer " + token);
                }
            }
            return next.execute(request, body);
        };
    }
}
