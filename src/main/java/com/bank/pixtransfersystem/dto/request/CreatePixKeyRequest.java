package com.bank.pixtransfersystem.dto.request;

import com.bank.pixtransfersystem.domain.enums.PixKeyType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreatePixKeyRequest(
        @NotNull UUID accountId,
        @NotNull PixKeyType keyType,
        String keyValue
) {}
