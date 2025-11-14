-- Add check constraints to rollout_rule table

-- Percentage must be between 0 and 100
ALTER TABLE rollout_rule
ADD CONSTRAINT ck_rollout_rule_percentage_range
CHECK (percentage IS NULL OR (percentage >= 0 AND percentage <= 100));

-- Value percentage must be between 0 and 100
ALTER TABLE rollout_rule
ADD CONSTRAINT ck_rollout_rule_value_percentage_range
CHECK (value_percentage IS NULL OR (value_percentage >= 0 AND value_percentage <= 100));

-- Priority must be non-negative
ALTER TABLE rollout_rule
ADD CONSTRAINT ck_rollout_rule_priority_non_negative
CHECK (priority >= 0);

-- Start time must be before end time
ALTER TABLE rollout_rule
ADD CONSTRAINT ck_rollout_rule_time_range
CHECK (start_at IS NULL OR end_at IS NULL OR start_at < end_at);

-- Create composite index for efficient rule lookup by flag and priority
CREATE INDEX idx_rollout_rule_flag_priority ON rollout_rule(feature_flag_id, priority);
