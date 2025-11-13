package com.azatkhaliullin.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SecurityConstants {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String[] PUBLIC_ENDPOINTS = {
            "/auth/login",
            "/jwk/.well-known/jwks.json",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };
}
