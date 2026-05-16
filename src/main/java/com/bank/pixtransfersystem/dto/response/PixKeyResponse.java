package com.bank.pixtransfersystem.dto.response;

import com.bank.pixtransfersystem.domain.entity.PixKey;
import com.bank.pixtransfersystem.domain.enums.PixKeyType;

import java.util.UUID;

public record PixKeyResponse(
        UUID id,
        UUID accountId,
        PixKeyType keyType,
        String keyValue,
        boolean active
) {
    public static PixKeyResponse from(PixKey k) {
        return new PixKeyResponse(k.getId(), k.getAccount().getId(),
                k.getKeyType(), k.getKeyValue(), k.isActive());
    }
}
