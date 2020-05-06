--saveAll()
INSERT INTO auditoriums (name) VALUES ('one');
INSERT INTO auditoriums (name) VALUES ('new');
INSERT INTO courses (name) VALUES ('one');
INSERT INTO groups (name) VALUES ('one');
INSERT INTO professors (first_name, last_name) VALUES ('one', 'one');
INSERT INTO professors (first_name, last_name) VALUES ('new', 'new');

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (true, 'MONDAY', 'SECOND', 1, 1, 1, 1);

INSERT INTO schedules (template_id, on_date, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (1, '2020-09-07', 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedules (template_id, on_date, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (2, '2020-09-07', 'MONDAY', 'SECOND', 1, 1, 1, 1);

INSERT INTO schedules (template_id, on_date, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (1, '2020-09-14', 'MONDAY', 'FIRST', 1, 1, 1, 1);
