package com.bank.pixtransfersystem.service;

import com.bank.pixtransfersystem.domain.entity.Account;
import com.bank.pixtransfersystem.domain.entity.PixTransaction;
import com.bank.pixtransfersystem.domain.enums.TransactionStatus;
import com.bank.pixtransfersystem.dto.request.PixTransferRequest;
import com.bank.pixtransfersystem.dto.response.PixTransactionResponse;
import com.bank.pixtransfersystem.exception.BusinessException;
import com.bank.pixtransfersystem.exception.InsufficientBalanceException;
import com.bank.pixtransfersystem.kafka.producer.PixTransactionProducer;
import com.bank.pixtransfersystem.repository.PixTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final PixTransactionRepository transactionRepository;
    private final AccountService accountService;
    private final PixKeyService pixKeyService;
    private final PixTransactionProducer producer;

    @Transactional
    public PixTransactionResponse initiate(PixTransferRequest request) {
        // Idempotência: retorna a transação existente se a chave já foi usada
        return transactionRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(existing -> {
                    log.info("Transação idempotente retornada: key={}", request.idempotencyKey());
                    return PixTransactionResponse.from(existing);
                })
                .orElseGet(() -> createAndPublish(request));
    }

    private PixTransactionResponse createAndPublish(PixTransferRequest request) {
        Account sender = accountService.getOrThrow(request.senderAccountId());
        Account receiver = pixKeyService.resolveAccountByKey(request.receiverPixKey());

        if (sender.getId().equals(receiver.getId())) {
            throw new BusinessException("Não é possível transferir para a mesma conta");
        }
        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(request.amount(), sender.getBalance());
        }

        PixTransaction transaction = PixTransaction.builder()
                .idempotencyKey(request.idempotencyKey())
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(request.amount())
                .description(request.description())
                .status(TransactionStatus.PENDING)
                .build();

        PixTransaction saved = transactionRepository.save(transaction);
        producer.sendTransactionEvent(saved.getId());

        log.info("Transação PIX criada: id={}, valor={}", saved.getId(), request.amount());
        return PixTransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PixTransactionResponse findById(UUID id) {
        return PixTransactionResponse.from(getOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<PixTransactionResponse> findBySenderAccountId(UUID accountId) {
        return transactionRepository.findBySenderAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(PixTransactionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PixTransactionResponse> findByReceiverAccountId(UUID accountId) {
        return transactionRepository.findByReceiverAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(PixTransactionResponse::from).toList();
    }

    public PixTransaction getOrThrow(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new com.bank.pixtransfersystem.exception.ResourceNotFoundException(
                        "Transação não encontrada: " + id));
    }
}
