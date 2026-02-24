-- Create wallet transactions table
CREATE TABLE wallet_transactions (
    id BIGSERIAL PRIMARY KEY,
    tourist_id BIGINT NOT NULL REFERENCES tourists(id) ON DELETE CASCADE,
    amount NUMERIC(12, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    description VARCHAR(500) NOT NULL,
    reference_id VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallet_transactions_tourist_id ON wallet_transactions(tourist_id);
CREATE INDEX idx_wallet_transactions_created_at ON wallet_transactions(created_at DESC);
