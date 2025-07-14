package com.banking.transactionservice.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class AccountClient {

    private final RestTemplate restTemplate;

    @Value("${services.account-service.url}")
    private String accountServiceUrl;

    public AccountClient() {
        this.restTemplate = new RestTemplate();
    }

    public boolean validateAndProcessTransfer(String origin, String destination, BigDecimal amount, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = new HashMap<>();
        payload.put("originAccount", origin);
        payload.put("destinationAccount", destination);
        payload.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                accountServiceUrl + "/accounts/transfer",
                request,
                Void.class
        );

        return response.getStatusCode().is2xxSuccessful();
    }
}
