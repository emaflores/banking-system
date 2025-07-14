package com.banking.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class JwtAuthFilter implements WebFilter {

    private static final String SECRET_KEY = "my-secret-jwt-key-that-is-long-enough-123456";

    private boolean isSecuredPath(String path) {
        return path.startsWith("/accounts") || path.startsWith("/transactions");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (!isSecuredPath(request.getURI().getPath())) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Falta token"));
        }

        String token = authHeader.substring(7);

        try {
            Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (JwtException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido"));
        }

        return chain.filter(exchange);
    }
}
