package com.ownorg.ai.a2a.config.user;

import io.a2a.server.auth.UnauthenticatedUser;
import io.a2a.server.auth.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;

public final class A2ADefaultUserProvider implements A2AUserProvider {
    @Override
    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        User user;
        if (auth == null || !auth.isAuthenticated()) {
            user = UnauthenticatedUser.INSTANCE;
        } else {
            Object principal = auth.getPrincipal();
            user = new User() {
                @Override
                public boolean isAuthenticated() {
                    return auth.isAuthenticated();
                }

                @Override
                public String getUsername() {
                    if (principal instanceof UserDetails ud) {
                        return ud.getUsername();
                    }
                    if (principal instanceof Principal p) {
                        return p.getName();
                    }
                    return principal.toString();
                }
            };
        }
        return user;
    }
}
