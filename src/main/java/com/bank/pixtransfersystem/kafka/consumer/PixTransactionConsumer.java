package com.bank.pixtransfersystem.kafka.consumer;

import com.bank.pixtransfersystem.service.TransactionProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixTransactionConsumer {

    private final TransactionProcessingService transactionProcessingService;

    @KafkaListener(topics = "pix.transactions", groupId = "pix-group")
    public void consume(String message) {
        UUID transactionId = UUID.fromString(message.trim());
        log.info("Evento recebido do Kafka: transactionId={}", transactionId);
        try {
            transactionProcessingService.process(transactionId);
        } catch (Exception e) {
            log.error("Erro ao processar transação: transactionId={}", transactionId, e);
        }
    }
}
