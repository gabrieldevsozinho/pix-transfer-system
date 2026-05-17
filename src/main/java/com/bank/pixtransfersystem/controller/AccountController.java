package com.bank.pixtransfersystem.controller;

import com.bank.pixtransfersystem.dto.request.CreateAccountRequest;
import com.bank.pixtransfersystem.dto.response.AccountResponse;
import com.bank.pixtransfersystem.dto.response.LedgerEntryResponse;
import com.bank.pixtransfersystem.service.AccountService;
import com.bank.pixtransfersystem.service.LedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Validated
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

    @PostMapping("/{id}/deposit")
    @Operation(summary = "Depositar valor na conta")
    public AccountResponse deposit(
            @PathVariable UUID id,
            @RequestParam @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01") BigDecimal amount) {
        return accountService.deposit(id, amount);
    }

    @GetMapping("/{id}/ledger")
    @Operation(summary = "Extrato do ledger por conta")
    public List<LedgerEntryResponse> ledger(@PathVariable UUID id) {
        return ledgerService.findByAccountId(id);
    }
}
