package com.bank.pixtransfersystem.controller;

import com.bank.pixtransfersystem.dto.request.CreateAccountRequest;
import com.bank.pixtransfersystem.dto.response.AccountResponse;
import com.bank.pixtransfersystem.dto.response.LedgerEntryResponse;
import com.bank.pixtransfersystem.service.AccountService;
import com.bank.pixtransfersystem.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Gerenciamento de contas")
public class AccountController {

    private final AccountService accountService;
    private final LedgerService ledgerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar conta")
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.create(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar conta")
    public AccountResponse findById(@PathVariable UUID id) {
        return accountService.findById(id);
    }

    @GetMapping("/{id}/ledger")
    @Operation(summary = "Extrato do ledger por conta")
    public List<LedgerEntryResponse> ledger(@PathVariable UUID id) {
        return ledgerService.findByAccountId(id);
    }
}
