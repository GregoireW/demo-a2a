package com.ownorg.ai.a2a.config.user;

import io.a2a.server.auth.User;

public sealed interface A2AUserProvider permits A2AAnonymousUserProvider, A2ADefaultUserProvider {
    User getUser();
}
