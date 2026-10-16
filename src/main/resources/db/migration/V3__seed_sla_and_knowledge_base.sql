-- =============================================================================
-- V3__seed_sla_and_knowledge_base.sql
-- Seed default SLA policies, categories, knowledge articles, and AI configs (MySQL Compatible)
-- =============================================================================

-- 1. Insert Categories and Subcategories
INSERT IGNORE INTO ticket_categories (id, name, code, description, is_ai_enabled, is_sensitive) VALUES
(1, 'Account & Login', 'ACCOUNT_LOGIN', 'Login issues, password resets, profile management', TRUE, FALSE),
(2, 'Billing & Subscription', 'BILLING', 'Payment issues, invoices, refunds, subscription plans', FALSE, TRUE),
(3, 'Playback & Streaming', 'PLAYBACK', 'Video buffering, audio sync, DRM, device playback', TRUE, FALSE),
(4, 'Partner Integration', 'PARTNER_INTEGRATION', 'Partner APIs, SDKs, feed ingestion, analytics', FALSE, FALSE),
(5, 'Production House Assets', 'PRODUCTION_ASSETS', 'Master video uploads, subtitles, DRM encoding', FALSE, FALSE);

INSERT IGNORE INTO ticket_sub_categories (category_id, name, code, description) VALUES
(1, 'Password Reset', 'PWD_RESET', 'Forgot password or reset link expired'),
(1, 'OTP Not Received', 'OTP_FAIL', 'SMS/Email OTP verification failure'),
(2, 'Payment Failed', 'PAYMENT_FAIL', 'Card or UPI charge failed'),
(2, 'Refund Request', 'REFUND_REQ', 'Customer requested subscription refund'),
(3, 'Video Buffering', 'BUFFERING', 'Constant loading spinner during playback'),
(3, 'Audio Delay', 'AUDIO_SYNC', 'Audio out of sync with video'),
(4, 'API Authentication', 'API_AUTH', 'Partner API key or OAuth errors'),
(5, 'Upload Failure', 'UPLOAD_FAIL', 'Asset upload chunk timeout');

-- 2. Insert Default SLA Policies
INSERT IGNORE INTO sla_policies (name, priority, source_portal, first_response_time_minutes, resolution_time_minutes, is_active) VALUES
('Urgent Priority SLA', 'URGENT', NULL, 15, 120, TRUE),      -- 15m response, 2h resolution
('High Priority SLA', 'HIGH', NULL, 60, 480, TRUE),          -- 1h response, 8h resolution
('Medium Priority SLA', 'MEDIUM', NULL, 180, 1440, TRUE),    -- 3h response, 24h resolution
('Low Priority SLA', 'LOW', NULL, 720, 4320, TRUE);          -- 12h response, 72h resolution

-- 3. Insert Knowledge Base Articles
INSERT IGNORE INTO knowledge_articles (title, category, content, keywords, status, version, created_by_user_id) VALUES
(
    'How to Reset Your Filmytell Account Password',
    'ACCOUNT_LOGIN',
    'To reset your password: 1. Click on "Forgot Password" on the login screen. 2. Enter your registered email address. 3. Check your inbox for the password reset link (valid for 15 minutes). 4. Enter a strong password with at least 8 characters including one number and one special character.',
    'forgot password, reset password, login issue, account recovery',
    'PUBLISHED',
    1,
    1
),
(
    'Troubleshooting Video Playback and Buffering Issues',
    'PLAYBACK',
    'If you encounter buffering: 1. Verify your internet connection speed is at least 10 Mbps for HD or 25 Mbps for 4K. 2. Clear your browser cache and cookies or restart the Filmytell App. 3. Check if hardware acceleration is enabled in your browser settings. 4. Disable VPN or proxy connections.',
    'buffering, video slow, playback error, loading, black screen',
    'PUBLISHED',
    1,
    1
),
(
    'Supported Devices and Browsers for Filmytell Streaming',
    'GENERAL_INFORMATION',
    'Filmytell supports Chrome 90+, Firefox 88+, Safari 14+, Edge 90+, Android 8.0+, iOS 14+, Android TV, Apple TV, Amazon Fire TV, and Samsung Tizen TVs manufactured from 2020 onwards.',
    'supported devices, tv app, browser compatibility, fire tv, android tv',
    'PUBLISHED',
    1,
    1
);

-- 4. Insert Default AI Configurations
INSERT IGNORE INTO ai_configurations (config_key, config_value, description) VALUES
('AI_AUTO_RESOLVE_ENABLED', 'true', 'Global toggle for AI auto resolution'),
('AI_CONFIDENCE_THRESHOLD', '0.85', 'Minimum confidence score required for auto-resolution (0.0 to 1.0)'),
('AI_MAX_DAILY_AUTO_RESOLVES', '1000', 'Safety ceiling for daily automated resolutions'),
('AI_MODEL_NAME', 'gemini-1.5-pro', 'Active LLM model used for classification and synthesis');
