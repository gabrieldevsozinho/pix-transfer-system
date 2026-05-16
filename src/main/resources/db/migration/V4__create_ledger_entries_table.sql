CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id  UUID            NOT NULL REFERENCES pix_transactions(id),
    account_id      UUID            NOT NULL REFERENCES accounts(id),
    entry_type      VARCHAR(10)     NOT NULL,   -- DEBIT, CREDIT
    amount          NUMERIC(15,2)   NOT NULL,
    balance_after   NUMERIC(15,2)   NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ledger_entries_account_id     ON ledger_entries(account_id);
CREATE INDEX idx_ledger_entries_transaction_id ON ledger_entries(transaction_id);
