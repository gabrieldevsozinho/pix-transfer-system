# PIX Transfer System

API que simula o fluxo completo do PIX: criação de chave, transferência, devolução e extrato.

## Stack
- Java 21 + Spring Boot 3.3
- PostgreSQL (ledger financeiro com dupla entrada)
- Redis (cache de chaves PIX)
- Apache Kafka (processamento assíncrono de transações)
- Flyway (migrations)
- OpenAPI/Swagger

## Funcionalidades
- Cadastro e consulta de chaves PIX (CPF, e-mail, telefone, chave aleatória)
- Início de transferência com validação de saldo
- Processamento assíncrono via Kafka
- Devolução de transferência
- Consulta de extrato
- Idempotência por chave de transação

## Como rodar

### Pré-requisitos
```bash
# Na raiz de projetos-banco, subir a infraestrutura
docker-compose up -d postgres redis kafka
```

### Rodando a aplicação
```bash
./mvnw spring-boot:run
```

### Endpoints
Acesse a documentação em: http://localhost:8080/swagger-ui.html

## Fluxo de uma transferência
```
POST /api/pix/transfer
        ↓
   Valida chave e saldo
        ↓
   Persiste como PENDENTE
        ↓
   Publica no Kafka (topic: pix.transactions)
        ↓
   Consumer processa e atualiza ledger
        ↓
   Status final: CONFIRMADA ou FALHA
```

## Estrutura do projeto
```
src/main/java/com/bank/pixtransfersystem/
├── controller/
├── service/
├── domain/
│   ├── entity/
│   └── enums/
├── repository/
├── kafka/
│   ├── producer/
│   └── consumer/
├── dto/
└── exception/
```
