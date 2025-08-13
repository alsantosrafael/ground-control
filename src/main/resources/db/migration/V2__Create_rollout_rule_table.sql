CREATE TABLE rollout_rule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    feature_flag_id BIGINT NOT NULL,
    attribute_key VARCHAR(255),
    attribute_value VARCHAR(255),
    percentage DOUBLE PRECISION,
    distribution_key_attribute VARCHAR(255),
    value_bool BOOLEAN,
    value_string TEXT,
    value_int INTEGER,
    value_percentage DOUBLE PRECISION,
    variant_name VARCHAR(255),
    start_at TIMESTAMP WITH TIME ZONE,
    end_at TIMESTAMP WITH TIME ZONE,
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_rollout_rule_feature_flag 
        FOREIGN KEY (feature_flag_id) REFERENCES feature_flag(id) ON DELETE CASCADE
);

CREATE INDEX idx_rollout_rule_feature_flag_id ON rollout_rule(feature_flag_id);
CREATE INDEX idx_rollout_rule_active ON rollout_rule(active);
CREATE INDEX idx_rollout_rule_priority ON rollout_rule(priority);
CREATE INDEX idx_rollout_rule_time_range ON rollout_rule(start_at, end_at);