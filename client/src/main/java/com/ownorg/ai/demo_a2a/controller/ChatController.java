package com.ownorg.ai.demo_a2a.controller;

import com.ownorg.ai.demo_a2a.services.AgentService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final AgentService agentService;

    public ChatController(AgentService agentService) {
        this.agentService = agentService;
    }

    public record Msg(String message) {}

    @PostMapping("/chat")
    public String chat(@RequestBody Msg message) {
        return agentService.callAgent(message.message());
    }
}
