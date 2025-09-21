CREATE TABLE shortcut_user (
    id       SERIAL PRIMARY KEY,
    login    TEXT   NOT NULL,
    password TEXT   NOT NULL,
    site     TEXT   UNIQUE NOT NULL
);
