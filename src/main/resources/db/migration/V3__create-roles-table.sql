CREATE TABLE user_roles (
    id uuid default gen_random_uuid(),
    role VARCHAR(100) NOT NULL,
    user_id uuid NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    PRIMARY KEY (id),
    CONSTRAINT fk_user
    FOREIGN KEY(user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
)