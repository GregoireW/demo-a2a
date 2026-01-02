package com.ownorg.ai.a2a.definition;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.spec.AgentCard;

public interface AgentDefinition extends AgentExecutor{
    AgentCard.Builder publicCard();

    default AgentCard.Builder extendedCard(){
        return null;
    }
}
