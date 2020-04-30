--save courses for saveAllProfessorsCourses()
INSERT INTO courses (name) VALUES ('one');
INSERT INTO courses (name) VALUES ('two');

--saveAll()
INSERT INTO professors (first_name, last_name) VALUES ('one', 'one');
INSERT INTO professors (first_name, last_name) VALUES ('two', 'two');
INSERT INTO professors (first_name, last_name) VALUES ('three', 'three');

--save more data for findAllAvailable()
INSERT INTO groups (name) VALUES ('one');
INSERT INTO auditoriums (name) VALUES ('auditorium');

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 1, 2);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'SECOND', 1, 1, 1, 3);
