package com.bank.pixtransfersystem.dto.response;

import com.bank.pixtransfersystem.domain.entity.LedgerEntry;
import com.bank.pixtransfersystem.domain.enums.EntryType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record LedgerEntryResponse(
        UUID id,
        UUID transactionId,
        UUID accountId,
        EntryType entryType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        LocalDateTime createdAt
) {
    public static LedgerEntryResponse from(LedgerEntry e) {
        return new LedgerEntryResponse(
                e.getId(), e.getTransaction().getId(), e.getAccount().getId(),
                e.getEntryType(), e.getAmount(), e.getBalanceAfter(), e.getCreatedAt()
        );
    }
}
