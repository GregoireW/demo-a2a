package com.ownorg.ai.demo_a2a.agent;

import com.ownorg.ai.a2a.definition.Agent;
import com.ownorg.ai.a2a.definition.AgentDefinition;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Agent("color")
public class ColorAgent implements AgentDefinition {


    @Override
    public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        // You are in a thread pool managed by A2A server, not the main http server thread pool.
        var part = context.getMessage().getParts().getLast();
        String text="color red";
        if (part instanceof TextPart tp) {
            text=tp.getText();
        }
        var color=extractColor(text);

        final TaskUpdater updater = new TaskUpdater(context, eventQueue);

        // No task exists yet, so submit the task
        if (context.getTask() == null) {
            updater.submit();
        }
        updater.startWork();

        for (int i=0;i<5;i++) {
            final TextPart progressPart = new TextPart("Progress "+(i+1)*20+"% - color is "+color, Map.of("Progress", (i+1)*20+"%", "Color", color));
            final List<Part<?>> progressParts = List.of(progressPart);
            // add the response as an artifact and complete the task
            updater.addArtifact(progressParts, null, null, null);

            try {
                Thread.sleep(3000); // Simulate long processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        updater.complete(new Message.Builder().role(Message.Role.AGENT)
                .parts(Collections.singletonList(new TextPart("Completed processing with color "+color)))
                .build());
    }

    private String extractColor(String text) {
        // Use regular expression to extract color (first element after the word "color")
        var matcher = Pattern.compile("color\\s+(\\w+)").matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "black";
    }

    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        throw new UnsupportedOperationError();
    }

    @Override
    public AgentCard.Builder extendedCard() {
        var securityScheme = new OpenIdConnectSecurityScheme.Builder().openIdConnectUrl("http://localhost:8080/.well-known/openid-configuration").build();

        return new AgentCard.Builder()
                .name("Color agent")
                .description("Paint something with a specified color")
                .version("1.0.0")
                .documentationUrl("http://example.com/color")
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .skills(Collections.singletonList(new AgentSkill.Builder()
                        .id("paint_house")
                        .name("Paint house")
                        .description("This will paint room per room with the specified color")
                        .tags(Collections.singletonList("paint"))
                        .examples(List.of("paint", "color red"))
                        .build()))
                .security(List.of(Map.of(OpenIdConnectSecurityScheme.OPENID_CONNECT,
                        List.of("oidc", "profile"))))
                .securitySchemes(Map.of(OpenIdConnectSecurityScheme.OPENID_CONNECT, securityScheme));
    }

    @Override
    public AgentCard.Builder publicCard() {
        var securityScheme = new OpenIdConnectSecurityScheme.Builder().openIdConnectUrl("http://localhost:8080/.well-known/openid-configuration").build();

        return new AgentCard.Builder()
                .name("Color agent")
                .description("Paint something with a specified color")
                .version("1.0.0")
                .documentationUrl("http://example.com/color")
                .defaultInputModes(Collections.singletonList("text"))
                .defaultOutputModes(Collections.singletonList("text"))
                .security(List.of(Map.of(OpenIdConnectSecurityScheme.OPENID_CONNECT,
                        List.of("oidc", "profile"))))
                .skills(List.of())
                .securitySchemes(Map.of(OpenIdConnectSecurityScheme.OPENID_CONNECT, securityScheme));
    }
}
