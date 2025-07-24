package com.banking.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationListener {

    @KafkaListener(topics = "transfer-completed", groupId = "notification-group")
    public void handleTransferCompleted(String message) {
        log.info("📩 Notificación recibida: {}", message);
        // Simular envío de email o guardar en base de datos, si se desea
    }
}
