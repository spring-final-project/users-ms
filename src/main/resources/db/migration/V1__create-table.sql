CREATE TABLE users (
	id uuid default gen_random_uuid(),
	name VARCHAR(100) NOT NULL,
	email VARCHAR(100) NOT NULL,
	password VARCHAR(255) NOT NULL,
	created_at TIMESTAMPTZ DEFAULT now(),
	last_updated TIMESTAMPTZ NOT NULL,
	PRIMARY KEY (id)
)