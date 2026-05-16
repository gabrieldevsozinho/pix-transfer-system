package com.bank.pixtransfersystem.service;

import com.bank.pixtransfersystem.domain.entity.Account;
import com.bank.pixtransfersystem.domain.entity.PixTransaction;
import com.bank.pixtransfersystem.domain.enums.TransactionStatus;
import com.bank.pixtransfersystem.exception.ResourceNotFoundException;
import com.bank.pixtransfersystem.repository.AccountRepository;
import com.bank.pixtransfersystem.repository.PixTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionProcessingService {

    private final PixTransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;

    @Transactional
    public void process(UUID transactionId) {
        PixTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transação não encontrada: " + transactionId));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            log.warn("Transação ignorada (não está PENDING): id={}, status={}", transactionId, transaction.getStatus());
            return;
        }

        transaction.setStatus(TransactionStatus.PROCESSING);
        transactionRepository.save(transaction);

        try {
            Account sender = accountRepository.findByIdForUpdate(transaction.getSenderAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta remetente não encontrada"));
            Account receiver = accountRepository.findByIdForUpdate(transaction.getReceiverAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conta destinatária não encontrada"));

            sender.setBalance(sender.getBalance().subtract(transaction.getAmount()));
            receiver.setBalance(receiver.getBalance().add(transaction.getAmount()));

            accountRepository.save(sender);
            accountRepository.save(receiver);

            ledgerService.recordTransfer(transaction, sender, receiver);

            transaction.setStatus(TransactionStatus.CONFIRMED);
            transactionRepository.save(transaction);

            log.info("Transação PIX confirmada: id={}, valor={}", transactionId, transaction.getAmount());

        } catch (Exception e) {
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            log.error("Transação PIX falhou: id={}", transactionId, e);
            throw e;
        }
    }
}
