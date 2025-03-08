create table if not exists authors
(
    id        bigserial,
    full_name varchar(255),
    primary key (id)
);

create table if not exists genres
(
    id   bigserial,
    name varchar(255),
    primary key (id)
);

create table if not exists books
(
    id        bigserial,
    title     varchar(255),
    author_id bigint references authors (id) on delete cascade,
    genre_id  bigint references genres (id) on delete cascade,
    primary key (id)
);

create table if not exists comments
(
    id        bigserial,
    text      varchar(255),
    book_id bigint references books (id) on delete cascade,
    primary key (id)
);

-- security пользователи
create table users (
    username varchar(50) not null primary key,
    password varchar(100) not null,
    enabled boolean not null
);
-- security права
create table authorities (
    id bigint auto_increment primary key,
    username varchar(50) not null,
    authority varchar(50) not null,
    foreign key (username) references users(username)
);