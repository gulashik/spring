drop table if exists users2;
create table if not exists users2
(
    id   serial
        primary key,
    name varchar(255) not null
);
