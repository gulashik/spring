--liquibase formatted sql

--changeset x:2025-04-01--0001-init-schema
CREATE TABLE IF NOT EXISTS authors
(
    id        SERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS books
(
    id        SERIAL PRIMARY KEY,
    title     VARCHAR(255),
    author_id BIGINT,
    genre_id  BIGINT,
    FOREIGN KEY (author_id) REFERENCES authors (id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments
(
    id           SERIAL PRIMARY KEY,
    comment_text VARCHAR(255),
    book_id      BIGINT,
    FOREIGN KEY (book_id) REFERENCES books (id) ON DELETE CASCADE
);