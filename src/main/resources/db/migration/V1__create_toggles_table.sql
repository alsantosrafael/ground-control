CREATE TABLE toggles (
    id UUID PRIMARY KEY,
    key VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    rules JSONB NOT NULL DEFAULT '[]',
    default_value TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_toggles_key ON toggles(key);
