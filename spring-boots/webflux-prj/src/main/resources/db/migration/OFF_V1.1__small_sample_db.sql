DROP TABLE IF EXISTS language CASCADE;
DROP TABLE IF EXISTS author CASCADE;
DROP TABLE IF EXISTS book CASCADE;
DROP TABLE IF EXISTS book_store CASCADE;
DROP TABLE IF EXISTS book_to_book_store CASCADE;
DROP TABLE IF EXISTS weekday CASCADE;


CREATE TABLE language (
                          id              NUMERIC(7)     NOT NULL PRIMARY KEY,
                          cd              CHAR(2)       NOT NULL,
                          description     VARCHAR(50)
);

CREATE TABLE author (
                        id              NUMERIC(7)     NOT NULL PRIMARY KEY,
                        first_name      VARCHAR(50),
                        last_name       VARCHAR(50)  NOT NULL,
                        date_of_birth   DATE,
                        year_of_birth   NUMERIC(7),
                        distinguished   NUMERIC(1)
);

CREATE TABLE book (
                      id              NUMERIC(7)     NOT NULL PRIMARY KEY,
                      author_id       NUMERIC(7)     NOT NULL,
                      title           VARCHAR(400) NOT NULL,
                      published_in    NUMERIC(7)     NOT NULL,
                      language_id     NUMERIC(7)     NOT NULL,

                      CONSTRAINT fk_book_author     FOREIGN KEY (author_id)   REFERENCES author(id),
                      CONSTRAINT fk_book_language   FOREIGN KEY (language_id) REFERENCES language(id)
);

CREATE TABLE book_store (
                            name            VARCHAR(400) NOT NULL UNIQUE
);

CREATE TABLE book_to_book_store (
                                    name            CHARACTER(400) NOT NULL,
                                    book_id         INTEGER       NOT NULL,
                                    stock           INTEGER,

                                    PRIMARY KEY(name, book_id),
                                    CONSTRAINT fk_b2bs_book_store FOREIGN KEY (name)        REFERENCES book_store (name) ON DELETE CASCADE,
                                    CONSTRAINT fk_b2bs_book       FOREIGN KEY (book_id)     REFERENCES book (id)         ON DELETE CASCADE
);

CREATE TABLE weekday (
                          id       NUMERIC(7)     NOT NULL PRIMARY KEY,
                          name     VARCHAR(50) NOT NULL
);
