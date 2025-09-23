CREATE TABLE shortcut (
    id       SERIAL PRIMARY KEY,
    url      TEXT   NOT NULL,
    shortcut TEXT   UNIQUE NOT NULL,
    user_id  INT REFERENCES shortcut_user(id) NOT NULL,
    total    BIGINT NOT NULL DEFAULT 0
);
