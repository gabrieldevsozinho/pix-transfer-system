package com.bank.pixtransfersystem.repository;

import com.bank.pixtransfersystem.domain.entity.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PixKeyRepository extends JpaRepository<PixKey, UUID> {
    Optional<PixKey> findByKeyValueAndActiveTrue(String keyValue);
    List<PixKey> findByAccountIdAndActiveTrue(UUID accountId);
    boolean existsByKeyValue(String keyValue);
}
