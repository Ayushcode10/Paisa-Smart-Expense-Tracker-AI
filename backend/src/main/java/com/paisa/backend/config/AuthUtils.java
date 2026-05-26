package com.paisa.backend.config;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class AuthUtils {

    public static Long getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken token) {
            return (Long) token.getDetails();  // userId stored as credentials
        }
        throw new RuntimeException("No authenticated user found");
    }

    public static String getCurrentPhone() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) return (String) auth.getPrincipal();
        throw new RuntimeException("No authenticated user found");
    }
}