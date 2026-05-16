package com.bank.pixtransfersystem.service;

import com.bank.pixtransfersystem.domain.entity.Account;
import com.bank.pixtransfersystem.dto.request.CreateAccountRequest;
import com.bank.pixtransfersystem.dto.response.AccountResponse;
import com.bank.pixtransfersystem.exception.BusinessException;
import com.bank.pixtransfersystem.exception.ResourceNotFoundException;
import com.bank.pixtransfersystem.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        if (accountRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já possui conta cadastrada: " + request.cpf());
        }
        Account account = Account.builder()
                .ownerName(request.ownerName())
                .cpf(request.cpf())
                .agency(request.agency())
                .accountNumber(request.accountNumber())
                .balance(BigDecimal.ZERO)
                .build();
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return AccountResponse.from(getOrThrow(id));
    }

    @Transactional
    public AccountResponse deposit(UUID id, BigDecimal amount) {
        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
        account.setBalance(account.getBalance().add(amount));
        return AccountResponse.from(accountRepository.save(account));
    }

    public Account getOrThrow(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
    }

    public Account getForUpdate(UUID id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + id));
    }
}
