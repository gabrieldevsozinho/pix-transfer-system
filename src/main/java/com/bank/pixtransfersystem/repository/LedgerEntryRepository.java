package com.bank.pixtransfersystem.repository;

import com.bank.pixtransfersystem.domain.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
    List<LedgerEntry> findByTransactionId(UUID transactionId);
}
