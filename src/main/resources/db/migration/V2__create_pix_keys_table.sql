CREATE TABLE pix_keys (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id  UUID            NOT NULL REFERENCES accounts(id),
    key_type    VARCHAR(20)     NOT NULL,   -- CPF, EMAIL, PHONE, RANDOM
    key_value   VARCHAR(255)    NOT NULL UNIQUE,
    active      BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pix_keys_key_value ON pix_keys(key_value);
CREATE INDEX idx_pix_keys_account_id ON pix_keys(account_id);
