package com.banking.transactionservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
public class AccountClient {

    private final RestTemplate restTemplate;

    @Value("${services.account-service.url}")
    private String accountServiceUrl;

    public AccountClient() {
        this.restTemplate = createRestTemplateWithTimeout();
    }
    
    private RestTemplate createRestTemplateWithTimeout() {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000);
        requestFactory.setConnectionRequestTimeout(5000);
        return new RestTemplate(requestFactory);
    }

    @CircuitBreaker(name = "account-service", fallbackMethod = "fallbackTransfer")
    @Retry(name = "account-service")
    @TimeLimiter(name = "account-service")
    public CompletableFuture<Boolean> validateAndProcessTransfer(String origin, String destination, BigDecimal amount, String token) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Calling account service for transfer: {} -> {}, amount: {}", origin, destination, amount);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = new HashMap<>();
            payload.put("originAccount", origin);
            payload.put("destinationAccount", destination);
            payload.put("amount", amount);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            try {
                ResponseEntity<Void> response = restTemplate.postForEntity(
                        accountServiceUrl + "/accounts/transfer",
                        request,
                        Void.class
                );
                
                boolean success = response.getStatusCode().is2xxSuccessful();
                log.info("Account service response: {}", success ? "SUCCESS" : "FAILED");
                return success;
                
            } catch (Exception e) {
                log.error("Error calling account service: {}", e.getMessage());
                throw e;
            }
        });
    }
    
    public CompletableFuture<Boolean> fallbackTransfer(String origin, String destination, BigDecimal amount, String token, Exception ex) {
        log.error("Transfer fallback triggered for {} -> {}, amount: {}, error: {}", 
                 origin, destination, amount, ex.getMessage());
        return CompletableFuture.completedFuture(false);
    }
}
