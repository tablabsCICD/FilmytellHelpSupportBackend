-- =============================================================================
-- V2__seed_roles_permissions_teams.sql
-- Seed initial roles, permissions, teams, default users, and portal API keys (MySQL Compatible)
-- =============================================================================

-- 1. Insert Roles
INSERT IGNORE INTO roles (id, name, description) VALUES
(1, 'ROLE_ADMIN', 'Super Administrator with full access'),
(2, 'ROLE_SUPPORT_MANAGER', 'Support Team Lead / Manager with delegation & assignment rights'),
(3, 'ROLE_SUPPORT_MEMBER', 'General Customer Support Specialist'),
(4, 'ROLE_TECHNICAL_SUPPORT_MEMBER', 'Technical & Engineering Support Specialist');

-- 2. Insert Permissions
INSERT IGNORE INTO permissions (id, name, description) VALUES
(1, 'TICKET_VIEW', 'View tickets and details'),
(2, 'TICKET_CREATE', 'Create internal tickets'),
(3, 'TICKET_UPDATE', 'Update ticket details'),
(4, 'TICKET_ASSIGN', 'Assign ticket to member or team'),
(5, 'TICKET_REASSIGN', 'Reassign ticket to another member'),
(6, 'TICKET_DELEGATE', 'Delegate task to another member'),
(7, 'TICKET_TAKE_BACK', 'Take back delegated task or override'),
(8, 'TICKET_RESOLVE', 'Mark ticket or task as resolved'),
(9, 'TICKET_CLOSE', 'Close ticket permanently'),
(10, 'TICKET_REOPEN', 'Reopen resolved ticket'),
(11, 'PRIORITY_UPDATE', 'Change ticket priority'),
(12, 'STATUS_UPDATE', 'Change ticket status directly'),
(13, 'TEAM_MANAGE', 'Manage teams and membership'),
(14, 'USER_MANAGE', 'Manage users and roles'),
(15, 'AI_MANAGE', 'Configure AI support module'),
(16, 'REPORT_VIEW', 'View analytics and reports'),
(17, 'AUDIT_VIEW', 'View audit timeline and history'),
(18, 'MESSAGE_SEND', 'Send conversation messages to customer'),
(19, 'NOTE_ADD', 'Add internal notes to tickets');

-- 3. Map Role Permissions
-- Admin gets all permissions (1-19)
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- Support Manager gets permissions 1..12, 16, 17, 18, 19
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 2, id FROM permissions WHERE id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 16, 17, 18, 19);

-- Support Member gets permissions 1, 2, 3, 6, 8, 12, 18, 19
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions WHERE id IN (1, 2, 3, 6, 8, 12, 18, 19);

-- Technical Support Member gets permissions 1, 3, 6, 8, 12, 18, 19
INSERT IGNORE INTO role_permissions (role_id, permission_id)
SELECT 4, id FROM permissions WHERE id IN (1, 3, 6, 8, 12, 18, 19);

-- 4. Insert Default Teams
INSERT IGNORE INTO teams (id, name, code, description, is_active) VALUES
(1, 'Support Team', 'SUPPORT_TEAM', 'Customer, Account, Billing, and General Support', TRUE),
(2, 'Technical Support Team', 'TECH_SUPPORT_TEAM', 'Playback, Buffering, Engineering, Bug & API issues', TRUE);

-- 5. Insert Default Users (Password: Password@123 -> standard BCrypt hash)
-- BCrypt for "Password@123" is: $2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy
INSERT IGNORE INTO users (id, username, email, password_hash, first_name, last_name, is_active) VALUES
(1, 'admin', 'admin@filmytell.com', '$2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy', 'System', 'Admin', TRUE),
(2, 'manager_rahul', 'rahul.manager@filmytell.com', '$2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy', 'Rahul', 'Verma', TRUE),
(3, 'support_alice', 'alice.support@filmytell.com', '$2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy', 'Alice', 'Smith', TRUE),
(4, 'support_bob', 'bob.support@filmytell.com', '$2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy', 'Bob', 'Johnson', TRUE),
(5, 'tech_charlie', 'charlie.tech@filmytell.com', '$2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy', 'Charlie', 'Davis', TRUE),
(6, 'tech_david', 'david.tech@filmytell.com', '$2a$10$wN1rYw79n9R08B6c2K1B8.4u8qg4D/XG5L34o4L2Ue0a3n8O7L5Wy', 'David', 'Miller', TRUE);

-- Map Users to Roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- rahul -> ROLE_SUPPORT_MANAGER
(3, 3), -- alice -> ROLE_SUPPORT_MEMBER
(4, 3), -- bob -> ROLE_SUPPORT_MEMBER
(5, 4), -- charlie -> ROLE_TECHNICAL_SUPPORT_MEMBER
(6, 4); -- david -> ROLE_TECHNICAL_SUPPORT_MEMBER

-- Map Users to Teams
INSERT IGNORE INTO team_members (team_id, user_id, role_in_team, is_active) VALUES
(1, 2, 'LEAD', TRUE),   -- rahul in Support Team
(1, 3, 'MEMBER', TRUE), -- alice in Support Team
(1, 4, 'MEMBER', TRUE), -- bob in Support Team
(2, 5, 'MEMBER', TRUE), -- charlie in Tech Team
(2, 6, 'MEMBER', TRUE); -- david in Tech Team

-- 6. Insert Portal API Keys
-- Raw Keys:
-- Filmytell: filmytell-portal-api-key-2026
-- Filmytell Partner: filmytell-partner-portal-api-key-2026
-- Production House: production-house-portal-api-key-2026
INSERT IGNORE INTO portal_api_keys (portal_name, api_key_hash, is_active, description) VALUES
('FILMYTELL', 'filmytell-portal-api-key-2026', TRUE, 'API Key for Filmytell Customer App & Web'),
('FILMYTELL_PARTNER', 'filmytell-partner-portal-api-key-2026', TRUE, 'API Key for Filmytell Partner Portal'),
('PRODUCTION_HOUSE', 'production-house-portal-api-key-2026', TRUE, 'API Key for Production House Portal');
