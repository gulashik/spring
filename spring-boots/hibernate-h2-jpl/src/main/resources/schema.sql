-- todo схема будет генериться при запуске

create table avatars
(
    id        bigserial,
    photo_url varchar(8000),
    primary key (id)
);

create table courses
(
    id   bigserial,
    name varchar(255),
    primary key (id)
);

create table students
(
    id        bigserial,
    name      varchar(255),
    avatar_id bigint references avatars (id),
    primary key (id)
);

create table emails
(
    id         bigserial,
    student_id bigint references students (id) on delete cascade,
    email      varchar(255),
    primary key (id)
);

create table student_courses
(
    student_id bigint references students (id) on delete cascade,
    course_id  bigint references courses (id),
    primary key (student_id, course_id)
);

-- DROP TABLE IF EXISTS employees_projects;
-- DROP TABLE IF EXISTS addresses;
-- DROP TABLE IF EXISTS departments;
-- DROP TABLE IF EXISTS projects;
-- DROP TABLE IF EXISTS employees;

create table addresses
(
    id   bigint auto_increment primary key,
    city varchar(255)
);

create table departments
(
    id   bigint auto_increment primary key,
    name varchar(255)
);

create table projects
(
    id   bigint auto_increment primary key,
    name varchar(255)
);


create table employees
(
    id            bigint auto_increment primary key,
    first_name    varchar(255),
    last_name     varchar(255),
    salary        numeric(38,2),
    address_id    bigint,
    department_id bigint,
    foreign key (address_id) references addresses (id) on delete cascade,
    foreign key (department_id) references departments (id) on delete cascade
);

create table employees_projects
(
    employee_id bigint,
    project_id  bigint,
    foreign key (employee_id) references employees (id) on delete cascade,
    foreign key (project_id) references projects (id) on delete cascade
);


create table categories
(
    id                 bigint auto_increment primary key,
    parent_category_id bigint references categories (id) on delete cascade,
    name               varchar(255)
);