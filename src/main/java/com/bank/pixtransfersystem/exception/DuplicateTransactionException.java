package com.bank.pixtransfersystem.exception;

public class DuplicateTransactionException extends RuntimeException {
    public DuplicateTransactionException(String idempotencyKey) {
        super("Transação já processada para a chave de idempotência: " + idempotencyKey);
    }
}
