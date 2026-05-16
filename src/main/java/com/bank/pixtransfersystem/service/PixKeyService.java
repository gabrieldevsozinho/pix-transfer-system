package com.bank.pixtransfersystem.service;

import com.bank.pixtransfersystem.domain.entity.Account;
import com.bank.pixtransfersystem.domain.entity.PixKey;
import com.bank.pixtransfersystem.domain.enums.PixKeyType;
import com.bank.pixtransfersystem.dto.request.CreatePixKeyRequest;
import com.bank.pixtransfersystem.dto.response.PixKeyResponse;
import com.bank.pixtransfersystem.exception.BusinessException;
import com.bank.pixtransfersystem.exception.ResourceNotFoundException;
import com.bank.pixtransfersystem.repository.PixKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PixKeyService {

    private static final String CACHE_PREFIX = "pix:key:";

    private final PixKeyRepository pixKeyRepository;
    private final AccountService accountService;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public PixKeyResponse create(CreatePixKeyRequest request) {
        if (pixKeyRepository.existsByKeyValue(resolveKeyValue(request))) {
            throw new BusinessException("Chave PIX já cadastrada");
        }

        Account account = accountService.getOrThrow(request.accountId());
        String keyValue = resolveKeyValue(request);

        PixKey pixKey = PixKey.builder()
                .account(account)
                .keyType(request.keyType())
                .keyValue(keyValue)
                .build();

        PixKey saved = pixKeyRepository.save(pixKey);
        cacheKey(keyValue, account.getId().toString());
        return PixKeyResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PixKeyResponse findByKeyValue(String keyValue) {
        PixKey pixKey = pixKeyRepository.findByKeyValueAndActiveTrue(keyValue)
                .orElseThrow(() -> new ResourceNotFoundException("Chave PIX não encontrada: " + keyValue));
        return PixKeyResponse.from(pixKey);
    }

    @Transactional(readOnly = true)
    public List<PixKeyResponse> findByAccountId(UUID accountId) {
        accountService.getOrThrow(accountId);
        return pixKeyRepository.findByAccountIdAndActiveTrue(accountId)
                .stream().map(PixKeyResponse::from).toList();
    }

    @Transactional
    public void delete(UUID id) {
        PixKey pixKey = pixKeyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chave PIX não encontrada: " + id));
        pixKey.setActive(false);
        pixKeyRepository.save(pixKey);
        redisTemplate.delete(CACHE_PREFIX + pixKey.getKeyValue());
    }

    public Account resolveAccountByKey(String keyValue) {
        // Tenta cache primeiro
        String cachedAccountId = redisTemplate.opsForValue().get(CACHE_PREFIX + keyValue);
        if (cachedAccountId != null) {
            return accountService.getOrThrow(UUID.fromString(cachedAccountId));
        }
        PixKey pixKey = pixKeyRepository.findByKeyValueAndActiveTrue(keyValue)
                .orElseThrow(() -> new ResourceNotFoundException("Chave PIX não encontrada: " + keyValue));
        cacheKey(keyValue, pixKey.getAccount().getId().toString());
        return pixKey.getAccount();
    }

    private void cacheKey(String keyValue, String accountId) {
        redisTemplate.opsForValue().set(CACHE_PREFIX + keyValue, accountId);
    }

    private String resolveKeyValue(CreatePixKeyRequest request) {
        if (request.keyType() == PixKeyType.RANDOM) {
            return UUID.randomUUID().toString();
        }
        if (request.keyValue() == null || request.keyValue().isBlank()) {
            throw new BusinessException("keyValue é obrigatório para o tipo " + request.keyType());
        }
        return request.keyValue();
    }
}
