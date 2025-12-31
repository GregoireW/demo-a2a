package com.ownorg.ai.demo_a2a.services;

import io.a2a.A2A;
import io.a2a.client.*;
import io.a2a.spec.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

@Service
@AllArgsConstructor
public class AgentService {

    private final Client client;

    public String callAgent(String input) {
        Message message = A2A.toUserMessage(input);
        CompletableFuture<String> messageResponse = new CompletableFuture<>();

        StringBuilder textBuilder = new StringBuilder();

        BiConsumer<ClientEvent, AgentCard> consumer = (event, agentCard) -> {
            switch (event) {
            case MessageEvent messageEvent ->{
                    Message responseMessage = messageEvent.getMessage();
                    extractFromPart(responseMessage.getParts(), textBuilder);
                    messageResponse.complete(textBuilder.toString());
                }
                case TaskEvent taskEvent -> textBuilder.append("Received task event: ").append(taskEvent.getTask().getId()).append("\n");
                case TaskUpdateEvent taskUpdateEvent ->{
                    var upd=taskUpdateEvent.getUpdateEvent();
                    switch (upd){
                        case TaskArtifactUpdateEvent artifactUpdateEvent -> {
                            textBuilder.append("Task artifact updated: \n");
                            extractFromPart(artifactUpdateEvent.getArtifact().parts(), textBuilder);
                        }
                        case TaskStatusUpdateEvent statusUpdateEvent -> {
                            var status = statusUpdateEvent.getStatus();
                            textBuilder.append("Task status updated: ").append(status).append("\n");
                            if (status.message()!=null) {
                                extractFromPart(status.message().getParts(), textBuilder);
                            }
                            if (status.state()== TaskState.COMPLETED) {
                                messageResponse.complete(textBuilder.toString());
                            }

                        }
                    }
                }
            default-> messageResponse.complete("Received client event: " + event.getClass().getSimpleName());
            }
        };

        client.sendMessage(message, List.of(consumer), messageResponse::completeExceptionally);

        return messageResponse.join();
    }

    private static void extractFromPart(List<Part<?>> parts, StringBuilder textBuilder) {
        if (parts != null) {
            for (Part<?> part : parts) {
                if (part instanceof TextPart textPart) {
                    textBuilder.append(textPart.getText());
                }
            }
        }
        textBuilder.append("\n");
    }
}
