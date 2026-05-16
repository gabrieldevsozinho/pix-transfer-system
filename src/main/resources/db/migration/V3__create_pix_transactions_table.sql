CREATE TABLE pix_transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(64)     NOT NULL UNIQUE,
    sender_account_id   UUID            NOT NULL REFERENCES accounts(id),
    receiver_account_id UUID            NOT NULL REFERENCES accounts(id),
    amount              NUMERIC(15,2)   NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    description         VARCHAR(255),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pix_transactions_sender    ON pix_transactions(sender_account_id);
CREATE INDEX idx_pix_transactions_receiver  ON pix_transactions(receiver_account_id);
CREATE INDEX idx_pix_transactions_status    ON pix_transactions(status);
CREATE INDEX idx_pix_transactions_idempotency ON pix_transactions(idempotency_key);
