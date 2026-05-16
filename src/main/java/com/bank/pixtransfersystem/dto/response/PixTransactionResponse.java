package com.bank.pixtransfersystem.dto.response;

import com.bank.pixtransfersystem.domain.entity.PixTransaction;
import com.bank.pixtransfersystem.domain.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PixTransactionResponse(
        UUID id,
        String idempotencyKey,
        UUID senderAccountId,
        UUID receiverAccountId,
        BigDecimal amount,
        TransactionStatus status,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PixTransactionResponse from(PixTransaction t) {
        return new PixTransactionResponse(
                t.getId(), t.getIdempotencyKey(),
                t.getSenderAccount().getId(), t.getReceiverAccount().getId(),
                t.getAmount(), t.getStatus(), t.getDescription(),
                t.getCreatedAt(), t.getUpdatedAt()
        );
    }
}
