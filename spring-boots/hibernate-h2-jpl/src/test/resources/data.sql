insert into avatars(photo_url)
values ('photoUrl_01'), ('photoUrl_02'), ('photoUrl_03'), ('photoUrl_04'), ('photoUrl_05'),
       ('photoUrl_06'), ('photoUrl_07'), ('photoUrl_08'), ('photoUrl_09'), ('photoUrl_10');

insert into courses(name)
values ('course_name_01'), ('course_name_02'), ('course_name_03'), ('course_name_04'), ('course_name_05'),
       ('course_name_06'), ('course_name_07'), ('course_name_08'), ('course_name_09'), ('course_name_10'), ('not_used_11');

insert into students(name, avatar_id)
values ('student_01', 1), ('student_02', 2), ('student_03', 3), ('student_04', 4), ('student_05', 5),
       ('student_06', 6), ('student_07', 7), ('student_08', 8), ('student_09', 9), ('student_10', 10);


insert into emails(email, student_id)
values ('email_01', 1), ('email_02', 1), ('email_03', 2), ('email_04', 2), ('email_05', 3), ('email_06', 4),
       ('email_07', 5), ('email_08', 6), ('email_09', 7), ('email_10', 8), ('email_11', 9), ('email_12', 10);


insert into student_courses(student_id, course_id)
values (1, 1),   (1, 2),   (1, 3),
       (2, 2),   (2, 4),   (2, 5),
       (3, 3),   (3, 6),   (3, 7),
       (4, 4),   (4, 8),   (4, 9),
       (5, 5),   (5, 10),  (5, 1),
       (6, 6),   (6, 2),   (6, 3),
       (7, 7),   (7, 4),   (7, 5),
       (8, 8),   (8, 6),   (8, 7),
       (9, 9),   (9, 8),   (9, 10),
       (10, 10), (10, 1),  (10, 2);

INSERT INTO addresses (city) VALUES ('Saratov'), ('Omsk'), ('Moscow');
INSERT INTO departments (name) VALUES ('IT'), ('AHO');
INSERT INTO projects (name) VALUES ('Project #1'), ('Project #2'), ('Project #3'), ('Project #4');

INSERT INTO employees (first_name, last_name, salary, address_id, department_id)
VALUES ('fn1', 'ln1', 70000, 1, 1),
       ('fn2', 'ln2', 99998, 1, null),
       ('fn3', 'ln3', 30000, 1, 2),

       ('fn4', 'ln4', 170000, 2, 1),

       ('fn5', 'ln5', 120000, 3, 1),
       ('fn6', 'ln6', 100400, 3, 1),
       ('fn7', 'ln7', 100000, 3, 1),
       ('fn8', 'ln8', 1000000, 3, null);


INSERT INTO employees_projects (employee_id, project_id)
VALUES (1, 1), (1, 2), (1, 3),
       (2, 3), (2, 4),
       (4, 1), (4, 2), (4, 3), (4, 4);


INSERT INTO categories (parent_category_id, name)
VALUES (null, 'Parent category #1'), (null, 'Parent category #2'), (null, 'Parent category #3'),
       (1, 'Child category #1'), (2, 'Child category #2'), (3, 'Child category #3');