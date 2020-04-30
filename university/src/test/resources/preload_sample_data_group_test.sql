--saveAll()
INSERT INTO groups (name) VALUES ('one');
INSERT INTO groups (name) VALUES ('two');
INSERT INTO groups (name) VALUES ('three');

--save more data for findAllByProfessorAndCourse()
INSERT INTO courses (name) VALUES ('course');
INSERT INTO auditoriums (name) VALUES ('auditorium');
INSERT INTO professors (first_name, last_name) VALUES ('professor', 'one');
INSERT INTO professors (first_name, last_name) VALUES ('professor', 'two');

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 2, 2);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'SECOND', 1, 1, 3, 1);