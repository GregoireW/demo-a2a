package com.ownorg.ai.a2a.config;

import io.a2a.server.TransportMetadata;
import io.a2a.spec.TransportProtocol;

public class A2ATransportMetadata implements TransportMetadata {

    @Override
    public String getTransportProtocol() {
        return TransportProtocol.JSONRPC.asString();
    }
}
