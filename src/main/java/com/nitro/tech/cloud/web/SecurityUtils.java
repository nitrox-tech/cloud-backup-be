package com.nitro.tech.cloud.web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Web layer only: resolves the authenticated user from Spring Security.
 * Controllers should call this once per request and pass the id string into services; services stay
 * free of tokens/JWT and only enforce business rules and authorization for that principal.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** Internal application user id (JWT subject). */
    public static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        if (auth.getPrincipal() instanceof String id) {
            return id;
        }
        throw new IllegalStateException("Unexpected principal type");
    }
}
