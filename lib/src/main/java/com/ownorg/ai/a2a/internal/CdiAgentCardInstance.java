package com.ownorg.ai.a2a.internal;

import io.a2a.spec.AgentCard;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.util.TypeLiteral;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.List;

public class CdiAgentCardInstance implements Instance<AgentCard> {
    private final AgentCard agentCard;

    public CdiAgentCardInstance(AgentCard agentCard) {
        this.agentCard = agentCard;
    }

    @Override
    public Instance<AgentCard> select(Annotation... qualifiers) {
        return this;
    }

    @Override
    public <U extends AgentCard> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        return null;
    }

    @Override
    public <U extends AgentCard> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        return null;
    }

    @Override
    public boolean isUnsatisfied() {
        return agentCard==null;
    }

    @Override
    public boolean isAmbiguous() {
        return false;
    }

    @Override
    public void destroy(AgentCard instance) {
        // No-op
    }

    @Override
    public Handle<AgentCard> getHandle() {
        return new Handle<AgentCard>() {
            @Override
            public AgentCard get() {
                return agentCard;
            }

            @Override
            public Bean<AgentCard> getBean() {
                return null;
            }

            @Override
            public void destroy() {
                // No-op
            }

            @Override
            public void close() {
                // No-op
            }
        };
    }

    @Override
    public Iterable<? extends Handle<AgentCard>> handles() {
        return null;
    }

    @Override
    public AgentCard get() {
        return agentCard;
    }

    @Override
    @NonNull
    public Iterator<AgentCard> iterator() {
        return List.of(agentCard).iterator();
    }
}
