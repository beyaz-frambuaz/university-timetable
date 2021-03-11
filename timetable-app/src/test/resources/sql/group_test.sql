--saveAll()
INSERT INTO groups (id, name)
VALUES (1, 'one');
INSERT INTO groups (id, name)
VALUES (2, 'two');
INSERT INTO groups (id, name)
VALUES (3, 'three');

--save more data for findAllByProfessorAndCourse()
INSERT INTO courses (id, name)
VALUES (1, 'course');
INSERT INTO auditoriums (id, name)
VALUES (1, 'auditorium');
INSERT INTO professors (id, first_name, last_name)
VALUES (1, 'professor', 'one');
INSERT INTO professors (id, first_name, last_name)
VALUES (2, 'professor', 'two');

INSERT INTO schedule_templates (id, week_parity, day, period, auditorium_id,
                                course_id, group_id, professor_id)
VALUES (1, false, 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedule_templates (id, week_parity, day, period, auditorium_id,
                                course_id, group_id, professor_id)
VALUES (2, false, 'MONDAY', 'FIRST', 1, 1, 2, 2);

INSERT INTO schedule_templates (id, week_parity, day, period, auditorium_id,
                                course_id, group_id, professor_id)
VALUES (3, false, 'MONDAY', 'SECOND', 1, 1, 3, 1);