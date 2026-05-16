package com.bank.pixtransfersystem.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixTransactionProducer {

    static final String TOPIC = "pix.transactions";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendTransactionEvent(UUID transactionId) {
        String message = transactionId.toString();
        kafkaTemplate.send(TOPIC, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Falha ao publicar transação no Kafka: transactionId={}", transactionId, ex);
                    } else {
                        log.info("Transação publicada no Kafka: transactionId={}, offset={}",
                                transactionId, result.getRecordMetadata().offset());
                    }
                });
    }
}
