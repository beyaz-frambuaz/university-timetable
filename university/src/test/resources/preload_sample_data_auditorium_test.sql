--saveAll()
INSERT INTO auditoriums (name) VALUES ('one');
INSERT INTO auditoriums (name) VALUES ('two');
INSERT INTO auditoriums (name) VALUES ('three');

--save more data for findAvailableAuditoriums()
INSERT INTO courses (name) VALUES ('course');
INSERT INTO groups (name) VALUES ('group');
INSERT INTO professors (first_name, last_name) VALUES ('name', 'name');

INSERT INTO schedules (on_date, day, period, auditorium_id,
course_id, group_id, professor_id) VALUES ('2020-09-07', 'MONDAY', 'FIRST', 1,
                                           1, 1, 1);

INSERT INTO schedules (on_date, day, period, auditorium_id,
course_id, group_id, professor_id) VALUES ('2020-09-07', 'MONDAY', 'FIRST', 2, 1, 1, 1);

INSERT INTO schedules (on_date, day, period, auditorium_id,
course_id, group_id, professor_id) VALUES ('2020-09-07', 'MONDAY', 'SECOND', 3, 1, 1, 1);