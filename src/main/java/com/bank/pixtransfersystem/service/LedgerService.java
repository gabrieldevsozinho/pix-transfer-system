package com.bank.pixtransfersystem.service;

import com.bank.pixtransfersystem.domain.entity.Account;
import com.bank.pixtransfersystem.domain.entity.LedgerEntry;
import com.bank.pixtransfersystem.domain.entity.PixTransaction;
import com.bank.pixtransfersystem.domain.enums.EntryType;
import com.bank.pixtransfersystem.dto.response.LedgerEntryResponse;
import com.bank.pixtransfersystem.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final LedgerEntryRepository ledgerEntryRepository;

    @Transactional
    public void recordTransfer(PixTransaction transaction, Account sender, Account receiver) {
        BigDecimal amount = transaction.getAmount();

        LedgerEntry debit = LedgerEntry.builder()
                .transaction(transaction)
                .account(sender)
                .entryType(EntryType.DEBIT)
                .amount(amount)
                .balanceAfter(sender.getBalance())
                .build();

        LedgerEntry credit = LedgerEntry.builder()
                .transaction(transaction)
                .account(receiver)
                .entryType(EntryType.CREDIT)
                .amount(amount)
                .balanceAfter(receiver.getBalance())
                .build();

        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> findByAccountId(UUID accountId) {
        return ledgerEntryRepository.findByAccountIdOrderByCreatedAtDesc(accountId)
                .stream().map(LedgerEntryResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> findByTransactionId(UUID transactionId) {
        return ledgerEntryRepository.findByTransactionId(transactionId)
                .stream().map(LedgerEntryResponse::from).toList();
    }
}
