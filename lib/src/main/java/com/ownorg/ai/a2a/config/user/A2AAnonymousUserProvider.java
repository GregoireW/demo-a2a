package com.ownorg.ai.a2a.config.user;

import io.a2a.server.auth.UnauthenticatedUser;
import io.a2a.server.auth.User;

public final class A2AAnonymousUserProvider implements A2AUserProvider {
    @Override
    public User getUser() {
        return UnauthenticatedUser.INSTANCE;
    }
}
