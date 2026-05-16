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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private PixTransactionRepository transactionRepository;
    @Mock private AccountService accountService;
    @Mock private PixKeyService pixKeyService;
    @Mock private PixTransactionProducer producer;

    @InjectMocks
    private TransactionService transactionService;

    private Account sender;
    private Account receiver;
    private UUID senderId;
    private UUID receiverId;

    @BeforeEach
    void setUp() {
        senderId = UUID.randomUUID();
        receiverId = UUID.randomUUID();
        sender = Account.builder().id(senderId).ownerName("Alice").cpf("11111111111")
                .agency("0001").accountNumber("111111").balance(new BigDecimal("1000.00")).build();
        receiver = Account.builder().id(receiverId).ownerName("Bob").cpf("22222222222")
                .agency("0001").accountNumber("222222").balance(new BigDecimal("500.00")).build();
    }

    @Test
    @DisplayName("Deve criar transação PIX com sucesso")
    void initiate_success() {
        String idempotencyKey = UUID.randomUUID().toString();
        PixTransferRequest request = new PixTransferRequest(
                senderId, "bob@pix.com", new BigDecimal("200.00"), "Pagamento", idempotencyKey);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountService.getOrThrow(senderId)).thenReturn(sender);
        when(pixKeyService.resolveAccountByKey("bob@pix.com")).thenReturn(receiver);

        PixTransaction saved = PixTransaction.builder()
                .id(UUID.randomUUID()).idempotencyKey(idempotencyKey)
                .senderAccount(sender).receiverAccount(receiver)
                .amount(new BigDecimal("200.00")).status(TransactionStatus.PENDING)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
        when(transactionRepository.save(any())).thenReturn(saved);

        PixTransactionResponse response = transactionService.initiate(request);

        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(response.amount()).isEqualByComparingTo("200.00");
        verify(producer).sendTransactionEvent(saved.getId());
    }

    @Test
    @DisplayName("Deve retornar transação existente ao receber chave de idempotência duplicada")
    void initiate_idempotent() {
        String idempotencyKey = "chave-unica-123";
        PixTransferRequest request = new PixTransferRequest(
                senderId, "bob@pix.com", new BigDecimal("200.00"), "Pagamento", idempotencyKey);

        PixTransaction existing = PixTransaction.builder()
                .id(UUID.randomUUID()).idempotencyKey(idempotencyKey)
                .senderAccount(sender).receiverAccount(receiver)
                .amount(new BigDecimal("200.00")).status(TransactionStatus.CONFIRMED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existing));

        PixTransactionResponse response = transactionService.initiate(request);

        assertThat(response.status()).isEqualTo(TransactionStatus.CONFIRMED);
        verify(transactionRepository, never()).save(any());
        verify(producer, never()).sendTransactionEvent(any());
    }

    @Test
    @DisplayName("Deve lançar InsufficientBalanceException quando saldo é insuficiente")
    void initiate_insufficientBalance() {
        String idempotencyKey = UUID.randomUUID().toString();
        PixTransferRequest request = new PixTransferRequest(
                senderId, "bob@pix.com", new BigDecimal("5000.00"), "Compra cara", idempotencyKey);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountService.getOrThrow(senderId)).thenReturn(sender);
        when(pixKeyService.resolveAccountByKey("bob@pix.com")).thenReturn(receiver);

        assertThatThrownBy(() -> transactionService.initiate(request))
                .isInstanceOf(InsufficientBalanceException.class)
                .hasMessageContaining("Saldo insuficiente");

        verify(transactionRepository, never()).save(any());
        verify(producer, never()).sendTransactionEvent(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao transferir para a mesma conta")
    void initiate_sameAccount() {
        String idempotencyKey = UUID.randomUUID().toString();
        PixTransferRequest request = new PixTransferRequest(
                senderId, "alice@pix.com", new BigDecimal("100.00"), "Auto", idempotencyKey);

        when(transactionRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.empty());
        when(accountService.getOrThrow(senderId)).thenReturn(sender);
        when(pixKeyService.resolveAccountByKey("alice@pix.com")).thenReturn(sender);

        assertThatThrownBy(() -> transactionService.initiate(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("mesma conta");
    }
}
