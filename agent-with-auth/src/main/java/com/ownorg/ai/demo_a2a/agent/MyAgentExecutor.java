package com.ownorg.ai.demo_a2a.agent;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.*;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@AllArgsConstructor
public class MyAgentExecutor implements AgentExecutor {

    @Override
    public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        // You are in a thread pool managed by A2A server, not the main http server thread pool.
        var part = context.getMessage().getParts().getLast();
        switch (part) {
            case TextPart tp when tp.getText().contains("task") -> {
                executeWithTaskUpdater(context, eventQueue);
            }
            case TextPart tp when tp.getText().contains("async") -> {
                executeWithTasks(context, eventQueue);
            }
            default -> {
                eventQueue.enqueueEvent(new Message.Builder()
                        .role(Message.Role.AGENT)
                        .parts(Collections.singletonList(new TextPart("Hello "+ SecurityContextHolder.getContext().getAuthentication().getName() + " This is " + Thread.currentThread().getName())))
                        .build());
            }
        }
    }

    private static void executeWithTasks(RequestContext context, EventQueue eventQueue) {
        var initialTask = new Task.Builder()
                .id(context.getTaskId())
                .contextId(context.getContextId())
                .status(new TaskStatus(TaskState.SUBMITTED, new Message.Builder().role(Message.Role.AGENT)
                        .parts(Collections.singletonList(new TextPart("Processing your async request..."+Thread.currentThread().getName())))
                        .build(), OffsetDateTime.now()))
                .build();
        eventQueue.enqueueEvent(initialTask);

        try {
            Thread.sleep(3000); // Simulate long processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        var intermediateTask = new TaskArtifactUpdateEvent.Builder()
                .taskId(context.getTaskId())
                .contextId(context.getContextId())
                .artifact(new Artifact.Builder()
                        .name("report")
                        .description(SecurityContextHolder.getContext().getAuthentication().getName()+ "this is an intermediate report.")
                        .artifactId("report-1")
                        .parts(Collections.singletonList(new TextPart("This is an intermediate report.")))
                        .build())
                .build();
        eventQueue.enqueueEvent(intermediateTask);

        try {
            Thread.sleep(3000); // Simulate long processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        var finalTask = new TaskStatusUpdateEvent.Builder()
                .taskId(context.getTaskId())
                .contextId(context.getContextId())
                .status(new TaskStatus(TaskState.COMPLETED, new Message.Builder().role(Message.Role.AGENT)
                        .parts(Collections.singletonList(new TextPart("Hello World - Async Response from " + Thread.currentThread().getName())))
                        .build(), OffsetDateTime.now()))
                .build();
        eventQueue.enqueueEvent(finalTask);
    }

    @Override
    public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        throw new UnsupportedOperationError();
    }


    public void executeWithTaskUpdater(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
        final TaskUpdater updater = new TaskUpdater(context, eventQueue);

        // mark the task as submitted and start working on it
        if (context.getTask() == null) {
            updater.submit();
        }
        updater.startWork();


        final TextPart responsePart = new TextPart("Processing task executor request for "+SecurityContextHolder.getContext().getAuthentication().getName()+" on "+Thread.currentThread().getName(), Map.of("Thread", Thread.currentThread().getName()));
        final List<Part<?>> parts = List.of(responsePart);
        // add the response as an artifact and complete the task
        updater.addArtifact(parts, null, null, null);


        try {
            Thread.sleep(3000); // Simulate long processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        final TextPart responseIntermediatePart = new TextPart("intermediate report task", null);
        final List<Part<?>> intermediateParts = List.of(responseIntermediatePart);
        // add the response as an artifact and complete the task
        updater.addArtifact(intermediateParts, null, null, null);

        try {
            Thread.sleep(3000); // Simulate long processing
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }


        updater.complete(new Message.Builder().role(Message.Role.AGENT)
                .parts(Collections.singletonList(new TextPart("Hello World - task Response")))
                .build());
    }
}
