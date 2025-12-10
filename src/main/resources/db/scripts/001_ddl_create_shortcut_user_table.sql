CREATE TABLE shortcut_user (
    id       INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    login    TEXT   CONSTRAINT uk_user_login UNIQUE,
    password TEXT   NOT NULL,
    site     TEXT   CONSTRAINT uk_user_site UNIQUE
);
