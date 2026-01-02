package com.ownorg.ai.a2a.internal;

import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;

import java.util.Map;
public record AgentStore(Map<String, JSONRPCHandler> agents, JSONRPCHandler wellKnownAgent) {
}
