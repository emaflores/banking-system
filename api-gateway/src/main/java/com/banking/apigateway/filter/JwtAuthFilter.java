package com.banking.apigateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.security.Key;

@Component
public class JwtAuthFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String secretKey;
    
    @PostConstruct
    public void validateConfig() {
        if (secretKey == null || secretKey.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters and not null");
        }
    }

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
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (JwtException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido"));
        }

        return chain.filter(exchange);
    }
}
