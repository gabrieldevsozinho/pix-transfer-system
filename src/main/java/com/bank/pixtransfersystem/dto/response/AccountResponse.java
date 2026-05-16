package com.bank.pixtransfersystem.dto.response;

import com.bank.pixtransfersystem.domain.entity.Account;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String ownerName,
        String cpf,
        String agency,
        String accountNumber,
        BigDecimal balance
) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getOwnerName(), a.getCpf(),
                a.getAgency(), a.getAccountNumber(), a.getBalance());
    }
}
