CREATE TABLE accounts (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_name      VARCHAR(150)    NOT NULL,
    cpf             VARCHAR(11)     NOT NULL UNIQUE,
    agency          VARCHAR(10)     NOT NULL,
    account_number  VARCHAR(20)     NOT NULL UNIQUE,
    balance         NUMERIC(15,2)   NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);
