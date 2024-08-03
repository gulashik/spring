-- drop table if exists users;
create table if not exists users
(
    id   serial
        primary key,
    name varchar(255) not null
);
