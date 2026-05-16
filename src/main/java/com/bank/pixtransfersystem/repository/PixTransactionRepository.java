package com.bank.pixtransfersystem.repository;

import com.bank.pixtransfersystem.domain.entity.PixTransaction;
import com.bank.pixtransfersystem.domain.enums.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PixTransactionRepository extends JpaRepository<PixTransaction, UUID> {
    Optional<PixTransaction> findByIdempotencyKey(String idempotencyKey);
    List<PixTransaction> findBySenderAccountIdOrderByCreatedAtDesc(UUID accountId);
    List<PixTransaction> findByReceiverAccountIdOrderByCreatedAtDesc(UUID accountId);
    List<PixTransaction> findByStatus(TransactionStatus status);
}
