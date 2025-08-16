-- Add conditions support to rollout_rule table
ALTER TABLE rollout_rule 
ADD COLUMN conditions JSONB;