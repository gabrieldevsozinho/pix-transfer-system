package com.bank.pixtransfersystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PixTransferRequest(
        @NotNull UUID senderAccountId,
        @NotBlank String receiverPixKey,
        @NotNull @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01") BigDecimal amount,
        String description,
        @NotBlank String idempotencyKey
) {}
