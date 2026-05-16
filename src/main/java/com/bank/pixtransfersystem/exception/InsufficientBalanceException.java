package com.bank.pixtransfersystem.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(BigDecimal requested, BigDecimal available) {
        super("Saldo insuficiente. Solicitado: R$ " + requested + ", Disponível: R$ " + available);
    }
}
