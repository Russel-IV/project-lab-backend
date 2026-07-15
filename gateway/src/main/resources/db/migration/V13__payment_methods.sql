CREATE TABLE IF NOT EXISTS payment_method (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    stripe_payment_method_id TEXT NOT NULL,
    brand VARCHAR(32) NOT NULL,
    last_four CHAR(4) NOT NULL,
    type VARCHAR(32) NOT NULL DEFAULT 'credit_card',
    expiry_month SMALLINT NOT NULL,
    expiry_year SMALLINT NOT NULL,

    CONSTRAINT fk_payment_method_user
        FOREIGN KEY (user_id)
        REFERENCES "user"(id)
        ON DELETE CASCADE,

    CONSTRAINT check_payment_method_expiry_month
        CHECK (expiry_month BETWEEN 1 AND 12)
);

CREATE INDEX idx_payment_method_user_id ON payment_method(user_id);
CREATE UNIQUE INDEX uq_payment_method_stripe_id ON payment_method(stripe_payment_method_id);
