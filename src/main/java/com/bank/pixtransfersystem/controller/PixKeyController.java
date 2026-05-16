package com.bank.pixtransfersystem.controller;

import com.bank.pixtransfersystem.dto.request.CreatePixKeyRequest;
import com.bank.pixtransfersystem.dto.response.PixKeyResponse;
import com.bank.pixtransfersystem.service.PixKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pix-keys")
@RequiredArgsConstructor
@Tag(name = "PIX Keys", description = "Gerenciamento de chaves PIX")
public class PixKeyController {

    private final PixKeyService pixKeyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar chave PIX")
    public PixKeyResponse create(@Valid @RequestBody CreatePixKeyRequest request) {
        return pixKeyService.create(request);
    }

    @GetMapping("/{keyValue}")
    @Operation(summary = "Consultar chave PIX")
    public PixKeyResponse findByKeyValue(@PathVariable String keyValue) {
        return pixKeyService.findByKeyValue(keyValue);
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Listar chaves PIX de uma conta")
    public List<PixKeyResponse> findByAccountId(@PathVariable UUID accountId) {
        return pixKeyService.findByAccountId(accountId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover chave PIX")
    public void delete(@PathVariable UUID id) {
        pixKeyService.delete(id);
    }
}
