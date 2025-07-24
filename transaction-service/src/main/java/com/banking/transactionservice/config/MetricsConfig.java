package com.banking.transactionservice.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter transferCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bank.transfers.total")
                .description("Total number of transfers")
                .tag("service", "transaction-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter transferSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bank.transfers.success")
                .description("Number of successful transfers")
                .tag("service", "transaction-service")
                .register(meterRegistry);
    }

    @Bean
    public Counter transferFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("bank.transfers.failure")
                .description("Number of failed transfers")
                .tag("service", "transaction-service")
                .register(meterRegistry);
    }

    @Bean
    public Timer transferProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("bank.transfer.duration")
                .description("Transfer processing time")
                .tag("service", "transaction-service")
                .register(meterRegistry);
    }
}