package com.ownorg.ai.a2a.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ownorg.ai.a2a.config.user.A2AUserProvider;
import com.ownorg.ai.a2a.internal.AgentStore;
import io.a2a.common.A2AHeaders;
import io.a2a.server.ServerCallContext;
import io.a2a.server.extensions.A2AExtensions;
import io.a2a.spec.*;
import io.a2a.spec.InternalError;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import io.a2a.util.Utils;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Flow;

import static io.a2a.transport.jsonrpc.context.JSONRPCContextKeys.HEADERS_KEY;
import static io.a2a.transport.jsonrpc.context.JSONRPCContextKeys.METHOD_NAME_KEY;

@RestController
@AllArgsConstructor
public class AgentController {
    private final AgentStore agentStore;
    private final A2AUserProvider a2AUserProvider;

    @GetMapping("/.well-known/agent-card.json")
    public ResponseEntity<AgentCard> getWellKnownAgentCard() {
        return agentCard(agentStore.wellKnownAgent());
    }

    @GetMapping("/.well-known/agents/{agent}.json")
    public ResponseEntity<AgentCard> getAgentCard(@PathVariable("agent") String agent) {
        return agentCard(agentStore.agents().get(agent));
    }

    public ResponseEntity<AgentCard> agentCard(JSONRPCHandler handler){
        if (handler == null) {
            return ResponseEntity.notFound().header("a2a-agent","No agent found").build();
        }
        return ResponseEntity.ok(handler.getAgentCard());
    }

    @PostMapping("/agent")
    public ResponseEntity<Object> handleWellKnownAgentRequest(@RequestBody JsonNode body, @RequestHeader HttpHeaders headers) {
        return handleAgentRequest(body, headers, agentStore.wellKnownAgent());
    }

    @PostMapping("/agents/{agent}")
    public ResponseEntity<Object> handleAgentRequest(
            @RequestBody JsonNode body,
            @RequestHeader HttpHeaders headers,
            @PathVariable("agent") String agent) {
        return handleAgentRequest(body, headers, agentStore.agents().get(agent));
    }

    public ResponseEntity<Object> handleAgentRequest(@RequestBody JsonNode body, @RequestHeader HttpHeaders headers, JSONRPCHandler handler) {
        if (handler == null) {
            return ResponseEntity.notFound().header("a2a-agent","No agent found").build();
        }
        try {
            JsonNode method = body != null ? body.get("method") : null;
            boolean streaming = method != null && (SendStreamingMessageRequest.METHOD.equals(method.asText())
                    || TaskResubscriptionRequest.METHOD.equals(method.asText()));

            var context = createCallContext(headers);
            context.getState().put(METHOD_NAME_KEY, method);

            if (streaming) {
                var streamingRequest = Utils.OBJECT_MAPPER.treeToValue(body, StreamingJSONRPCRequest.class);
                var streamingResponse = processStreamingRequest(handler, streamingRequest, context);
                SseEmitter emitter = new SseEmitter(Duration.ofMinutes(10).toMillis());
                var subscriber = new Flow.Subscriber<>() {
                    private Flow.Subscription subscription;

                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {
                        this.subscription = subscription;
                        subscription.request(1);
                    }

                    @Override
                    public void onNext(Object item) {
                        try {
                            emitter.send(SseEmitter.event().data(item));
                            subscription.request(1);
                        } catch (Exception e) {
                            subscription.cancel();
                            emitter.completeWithError(e);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        emitter.completeWithError(throwable);
                    }

                    @Override
                    public void onComplete() {
                        emitter.complete();
                    }
                };
                streamingResponse.subscribe(subscriber);
                return ResponseEntity.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(emitter);
            } else {
                var nonStreamingRequest = Utils.OBJECT_MAPPER.treeToValue(body, NonStreamingJSONRPCRequest.class);
                JSONRPCResponse<?> nonStreamingResponse = processNonStreamingRequest(handler, nonStreamingRequest, context);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(nonStreamingResponse);
            }
        } catch (Exception t) {
            JSONRPCErrorResponse error;
            if (t instanceof JsonProcessingException jpe) {
                error = handleError(jpe);
            } else {
                error = new JSONRPCErrorResponse(new InternalError(t.getMessage()));
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(error);
        }
    }

    private JSONRPCErrorResponse handleError(JsonProcessingException exception) {
        Object id = null;
        JSONRPCError jsonRpcError;
        if (exception.getCause() instanceof com.fasterxml.jackson.core.JsonParseException) {
            jsonRpcError = new JSONParseError();
        } else if (exception instanceof com.fasterxml.jackson.core.io.JsonEOFException) {
            jsonRpcError = new JSONParseError(exception.getMessage());
        } else if (exception instanceof MethodNotFoundJsonMappingException err) {
            id = err.getId();
            jsonRpcError = new MethodNotFoundError();
        } else if (exception instanceof InvalidParamsJsonMappingException err) {
            id = err.getId();
            jsonRpcError = new InvalidParamsError();
        } else if (exception instanceof IdJsonMappingException err) {
            id = err.getId();
            jsonRpcError = new InvalidRequestError();
        } else {
            jsonRpcError = new InvalidRequestError();
        }
        return new JSONRPCErrorResponse(id, jsonRpcError);
    }

    private JSONRPCResponse<?> processNonStreamingRequest(
            JSONRPCHandler jsonRpcHandler,
            NonStreamingJSONRPCRequest<?> request,
            ServerCallContext context) {
        return switch (request) {
            case GetTaskRequest req -> jsonRpcHandler.onGetTask(req, context);
            case CancelTaskRequest req -> jsonRpcHandler.onCancelTask(req, context);
            case SetTaskPushNotificationConfigRequest req -> jsonRpcHandler.setPushNotificationConfig(req, context);
            case GetTaskPushNotificationConfigRequest req -> jsonRpcHandler.getPushNotificationConfig(req, context);
            case SendMessageRequest req -> jsonRpcHandler.onMessageSend(req, context);
            case ListTaskPushNotificationConfigRequest req -> jsonRpcHandler.listPushNotificationConfig(req, context);
            case DeleteTaskPushNotificationConfigRequest req ->
                    jsonRpcHandler.deletePushNotificationConfig(req, context);
            case GetAuthenticatedExtendedCardRequest req ->
                    jsonRpcHandler.onGetAuthenticatedExtendedCardRequest(req, context);
        };
    }

    private Flow.Publisher<? extends JSONRPCResponse<?>> processStreamingRequest(
            JSONRPCHandler jsonRpcHandler,
            StreamingJSONRPCRequest<?> request,
            ServerCallContext context) {
        return switch (request) {
            case SendStreamingMessageRequest req -> jsonRpcHandler.onMessageSendStream(req, context);
            case TaskResubscriptionRequest req -> jsonRpcHandler.onResubscribeToTask(req, context);
        };
    }

    private ServerCallContext createCallContext(HttpHeaders headers) {
        var user = a2AUserProvider.getUser();
        Map<String, Object> state = new HashMap<>();
        Map<String, String> headersAsMap = new HashMap<>();
        headers.forEach((name, values) -> {
            if (!values.isEmpty()) {
                headersAsMap.put(name, values.getFirst());
            }
        });
        state.put(HEADERS_KEY, headersAsMap);

        List<String> extensionHeaderValues = headers.get(A2AHeaders.X_A2A_EXTENSIONS);
        Set<String> requestedExtensions = A2AExtensions.getRequestedExtensions(extensionHeaderValues);

        return new ServerCallContext(user, state, requestedExtensions);
    }
}
