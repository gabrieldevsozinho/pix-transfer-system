package com.bank.pixtransfersystem.controller;

import com.bank.pixtransfersystem.dto.request.PixTransferRequest;
import com.bank.pixtransfersystem.dto.response.LedgerEntryResponse;
import com.bank.pixtransfersystem.dto.response.PixTransactionResponse;
import com.bank.pixtransfersystem.service.LedgerService;
import com.bank.pixtransfersystem.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pix")
@RequiredArgsConstructor
@Tag(name = "PIX Transactions", description = "Transferências PIX")
public class PixTransactionController {

    private final TransactionService transactionService;
    private final LedgerService ledgerService;

    @PostMapping("/transfer")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Iniciar transferência PIX")
    public PixTransactionResponse transfer(@Valid @RequestBody PixTransferRequest request) {
        return transactionService.initiate(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar transferência PIX")
    public PixTransactionResponse findById(@PathVariable UUID id) {
        return transactionService.findById(id);
    }

    @GetMapping("/sent/{accountId}")
    @Operation(summary = "Transferências enviadas por uma conta")
    public List<PixTransactionResponse> sent(@PathVariable UUID accountId) {
        return transactionService.findBySenderAccountId(accountId);
    }

    @GetMapping("/received/{accountId}")
    @Operation(summary = "Transferências recebidas por uma conta")
    public List<PixTransactionResponse> received(@PathVariable UUID accountId) {
        return transactionService.findByReceiverAccountId(accountId);
    }

    @GetMapping("/{id}/ledger")
    @Operation(summary = "Entradas do ledger por transação")
    public List<LedgerEntryResponse> ledger(@PathVariable UUID id) {
        return ledgerService.findByTransactionId(id);
    }
}
