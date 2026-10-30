-- =============================================================================
-- Customer Feedback Portal (FeedbackHub) - Seed Data Migration V2
-- =============================================================================

-- Seed Categories
MERGE INTO categories (id, name, description, active) KEY(name) VALUES
(1, 'Product', 'Product-related feedback, features, and usability', TRUE),
(2, 'Service', 'Service quality, fulfillment, and responsiveness', TRUE),
(3, 'Support', 'Customer assistance and technical support experiences', TRUE),
(4, 'Other', 'General observations and miscellaneous feedback', TRUE);

-- Seed Demo Users
-- customer@example.com   / CustomerDemo123!
-- moderator@example.com  / ModeratorDemo123!
-- admin@example.com      / AdminDemo123!
MERGE INTO users (id, email, password_hash, role, active, created_at, version) KEY(email) VALUES
(1, 'customer@example.com', '$2a$12$yg5qN6fZyHOvYa8xEKMlmeLcXZyOCxn6.SAk6qJcmSFrgvFofa3VS', 'CUSTOMER', TRUE, CURRENT_TIMESTAMP, 0),
(2, 'moderator@example.com', '$2a$12$4m1B9gZc85g58k9mK.aEjeqZ7m7P/pYFpYp75dYx9.k1o9K0x6Pce', 'MODERATOR', TRUE, CURRENT_TIMESTAMP, 0),
(3, 'admin@example.com', '$2a$12$m7e7zE3yE7E4e9E9E9E9EedZ7m7P/pYFpYp75dYx9.k1o9K0x6Pce', 'ADMINISTRATOR', TRUE, CURRENT_TIMESTAMP, 0);

-- Seed Approved Feedback Samples
MERGE INTO feedback (id, customer_id, category_id, rating, text, status, private_feedback, created_at, updated_at, version) KEY(id) VALUES
(1, 1, 1, 5, 'The new UI navigation is so clean and easy to use! Great work on this update.', 'APPROVED', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(2, 1, 2, 4, 'Delivery was prompt and customer representatives answered all my questions clearly.', 'APPROVED', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(3, 1, 3, 5, 'Support team solved my issue within 10 minutes of submitting a ticket.', 'APPROVED', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
(4, 1, 1, 4, 'Solid features all around. Would love to see additional export formats in the future.', 'PENDING', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
