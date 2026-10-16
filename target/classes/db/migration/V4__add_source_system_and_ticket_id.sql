-- =============================================================================
-- V4__add_source_system_and_ticket_id.sql
-- Add source_system and source_ticket_id columns for Admin Portal integration (MySQL Compatible)
-- =============================================================================

ALTER TABLE tickets ADD COLUMN source_system VARCHAR(50);
ALTER TABLE tickets ADD COLUMN source_ticket_id VARCHAR(100);

CREATE UNIQUE INDEX uk_source_system_ticket_id ON tickets (source_system, source_ticket_id);
