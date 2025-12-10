CREATE TABLE shortcut (
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url      TEXT   CONSTRAINT uk_shortcut_url UNIQUE,
    shortcut TEXT   CONSTRAINT uk_shortcut_shortcut UNIQUE,
    user_id  INT REFERENCES shortcut_user(id) NOT NULL,
    total    BIGINT NOT NULL DEFAULT 0
);
