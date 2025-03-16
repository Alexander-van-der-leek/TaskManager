-- Insert default roles
INSERT INTO roles ( name, description) VALUES
    ( 'ADMIN', 'Administrator with full access'),
    ( 'DEVELOPER', 'Software developer role'),
    ( 'PRODUCT_OWNER', 'Product owner role'),
    ( 'SCRUM_MASTER', 'Scrum master role'),
    ( 'BUSINESS_ANALYST', 'Business analyst role'),
    ( 'QA', 'Quality assurance role');

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
