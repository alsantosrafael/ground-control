CREATE TABLE IF NOT EXISTS feature_flag (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    flag_type VARCHAR(20) NOT NULL DEFAULT 'BOOLEAN',
    default_bool_value BOOLEAN,
    default_int_value INTEGER,
    default_string_value TEXT,
    default_percentage_value DOUBLE PRECISION,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_feature_flag_code ON feature_flag(code);
CREATE INDEX idx_feature_flag_enabled ON feature_flag(enabled);
CREATE INDEX idx_feature_flag_due_at ON feature_flag(due_at);