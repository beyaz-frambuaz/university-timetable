--saveAll()
INSERT INTO auditoriums (id, name)
VALUES (1, 'one');
INSERT INTO auditoriums (id, name)
VALUES (2, 'two');
INSERT INTO auditoriums (id, name)
VALUES (3, 'three');

--save more data for findAvailableAuditoriums()
INSERT INTO courses (id, name)
VALUES (1, 'course');
INSERT INTO groups (id, name)
VALUES (1, 'group');
INSERT INTO professors (id, first_name, last_name)
VALUES (1, 'name', 'name');

INSERT INTO schedules (id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (1, '2020-09-07', 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedules (id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (2, '2020-09-07', 'MONDAY', 'FIRST', 2, 1, 1, 1);

INSERT INTO schedules (id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (3, '2020-09-07', 'MONDAY', 'SECOND', 3, 1, 1, 1);

INSERT INTO schedules (id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (4, '2020-09-08', 'MONDAY', 'SECOND', 3, 1, 1, 1);