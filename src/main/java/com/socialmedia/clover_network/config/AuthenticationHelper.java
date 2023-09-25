package com.socialmedia.clover_network.config;

import org.springframework.security.core.context.SecurityContextHolder;

public class AuthenticationHelper {

    public static String getUserIdFromContext() {
        AuthenticationUser principal = (AuthenticationUser) SecurityContextHolder.getContext().getAuthentication();
        return principal != null ? principal.getUserId() : null;
    }

    public static String getDomainFromContext() {
        AuthenticationUser principal = (AuthenticationUser) SecurityContextHolder.getContext().getAuthentication();
        return principal != null ? principal.getDomain() : null;
    }
}

