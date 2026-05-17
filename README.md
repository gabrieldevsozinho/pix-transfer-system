# PIX Transfer System

API que simula o fluxo completo do PIX: criação de chave, transferência e extrato — com arquitetura orientada a eventos.

## Stack
- Java 21 + Spring Boot 3.3
- PostgreSQL (ledger financeiro com dupla entrada)
- Redis (cache de chaves PIX)
- Apache Kafka (processamento assíncrono de transações)
- Flyway (migrations)
- OpenAPI/Swagger

## Funcionalidades
- Cadastro de contas e chaves PIX (CPF, e-mail, telefone, aleatória)
- Transferência com validação de saldo
- Idempotência — mesma chave não processa a transação duas vezes
- Processamento assíncrono via Kafka
- Ledger com dupla entrada (débito/crédito) por transação
- Pessimistic locking no saldo para evitar condição de corrida
- Estado da transação: `PENDING → PROCESSING → CONFIRMED/FAILED`
- Consulta de extrato por conta e por transação

---

## Como rodar

### Pré-requisitos
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado e rodando
- [Java 21](https://adoptium.net/) instalado
- [Maven](https://maven.apache.org/) instalado (ou usar o `./mvnw` incluso)

### 1. Subir a infraestrutura

Na pasta do projeto, execute:

```bash
docker-compose up -d
```

Aguarde todos os containers ficarem saudáveis. Você pode acompanhar com:

```bash
docker-compose ps
```

Todos devem aparecer como `running`. O Kafka demora ~20 segundos para inicializar.

### 2. Rodar a aplicação

```bash
./mvnw spring-boot:run
```

No Windows sem bash:
```cmd
mvnw.cmd spring-boot:run
```

A aplicação estará pronta quando aparecer no log:
```
Started PixTransferSystemApplication in X.XXX seconds
```

### 3. Acessar

| Recurso | URL |
|---------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Kafka UI | http://localhost:8090 |

---

## Testando o fluxo completo via Swagger

Acesse http://localhost:8080/swagger-ui.html e siga os passos:

### Passo 1 — Criar duas contas

`POST /api/accounts`
```json
{
  "ownerName": "Alice Silva",
  "cpf": "11111111111",
  "agency": "0001",
  "accountNumber": "111111"
}
```

`POST /api/accounts`
```json
{
  "ownerName": "Bob Santos",
  "cpf": "22222222222",
  "agency": "0001",
  "accountNumber": "222222"
}
```

Guarde os `id` retornados.

### Passo 2 — Cadastrar chave PIX para Bob

`POST /api/pix-keys`
```json
{
  "accountId": "<id-do-bob>",
  "keyType": "EMAIL",
  "keyValue": "bob@pix.com"
}
```

### Passo 3 — Depositar saldo para Alice

`POST /api/accounts/<id-da-alice>/deposit` *(ou ajuste o saldo direto no banco para testes)*

> **Atalho para testes:** conecte no PostgreSQL e execute:
> ```sql
> UPDATE accounts SET balance = 1000.00 WHERE cpf = '11111111111';
> ```

### Passo 4 — Realizar a transferência PIX

`POST /api/pix/transfer`
```json
{
  "senderAccountId": "<id-da-alice>",
  "receiverPixKey": "bob@pix.com",
  "amount": 200.00,
  "description": "Pagamento teste",
  "idempotencyKey": "chave-unica-abc-123"
}
```

A resposta virá com `status: "PENDING"`. O Kafka processa em seguida.

### Passo 5 — Verificar o resultado

Consulte a transação após alguns segundos:

`GET /api/pix/<id-da-transacao>`

O status deve estar `CONFIRMED`. Confira também o ledger:

`GET /api/pix/<id-da-transacao>/ledger`

Você verá as duas entradas: **DEBIT** na conta da Alice e **CREDIT** na conta do Bob.

---

## Testando idempotência

Envie exatamente o mesmo body do Passo 4 novamente (mesma `idempotencyKey`).
A API retornará a transação original sem criar uma nova — mesmo que o valor ou destinatário sejam diferentes.

---

## Conectar no PostgreSQL (opcional)

```
Host:     localhost
Porta:    5432
Banco:    pix_db
Usuário:  postgres
Senha:    postgres
```

Ferramenta recomendada: [DBeaver](https://dbeaver.io/) ou [TablePlus](https://tableplus.com/).

---

## Fluxo interno da transferência

```
POST /api/pix/transfer
        ↓
   Verifica idempotency_key (retorna existente se já processada)
        ↓
   Valida saldo do remetente
        ↓
   Persiste transação como PENDING
        ↓
   Publica ID no Kafka (topic: pix.transactions)
        ↓
   Consumer recebe o evento
        ↓
   Pessimistic lock nas duas contas
        ↓
   Debita remetente / Credita destinatário
        ↓
   Registra no ledger (DEBIT + CREDIT)
        ↓
   Status final: CONFIRMED ou FAILED
```

---

## Estrutura do projeto

```
src/main/java/com/bank/pixtransfersystem/
├── controller/          # Endpoints REST
├── service/             # Regras de negócio
├── domain/
│   ├── entity/          # Entidades JPA
│   └── enums/           # Status, tipos
├── repository/          # Spring Data JPA
├── kafka/
│   ├── producer/        # Publicação de eventos
│   └── consumer/        # Processamento assíncrono
├── dto/
│   ├── request/         # Payloads de entrada
│   └── response/        # Payloads de saída
└── exception/           # Erros e handler global
```

## Parar o ambiente

```bash
docker-compose down
```

Para remover também os dados do banco:
```bash
docker-compose down -v
```
