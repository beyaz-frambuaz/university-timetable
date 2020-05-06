--saveAll()
INSERT INTO auditoriums (name) VALUES ('one');
INSERT INTO auditoriums (name) VALUES ('two');
INSERT INTO auditoriums (name) VALUES ('three');

--save more data for findAvailableAuditoriums()
INSERT INTO courses (name) VALUES ('course');
INSERT INTO groups (name) VALUES ('group');
INSERT INTO professors (first_name, last_name) VALUES ('name', 'name');

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 2, 1, 1, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'SECOND', 3, 1, 1, 1);