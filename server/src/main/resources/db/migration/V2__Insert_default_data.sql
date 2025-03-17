-- Insert default roles
INSERT INTO roles ( name, description, created_at) VALUES
    ( 'ADMIN', 'Administrator with full access', CURRENT_TIMESTAMP),
    ( 'DEVELOPER', 'Software developer role', CURRENT_TIMESTAMP),
    ( 'PRODUCT_OWNER', 'Product owner role',CURRENT_TIMESTAMP),
    ( 'SCRUM_MASTER', 'Scrum master role', CURRENT_TIMESTAMP),
    ( 'BUSINESS_ANALYST', 'Business analyst role', CURRENT_TIMESTAMP),
    ( 'QA', 'Quality assurance role',CURRENT_TIMESTAMP);

-- Insert default task statuses
INSERT INTO task_statuses ( name, display_order) VALUES
    ( 'BACKLOG', 1),
    ( 'TODO', 2),
    ( 'IN_PROGRESS',  3),
    ( 'REVIEW',  4),
    ( 'DONE',  5);

-- Insert default task priorities
INSERT INTO task_priorities ( name, value) VALUES
    ( 'LOW',  1),
    ( 'MEDIUM',  2),
    ( 'HIGH',  3),
    ( 'CRITICAL',  4);
