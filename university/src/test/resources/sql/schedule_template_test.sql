--saveAll()
INSERT INTO auditoriums (id, name)
VALUES (1, 'one');
INSERT INTO courses (id, name)
VALUES (1, 'one');
INSERT INTO groups (id, name)
VALUES (1, 'one');
INSERT INTO professors (id, first_name, last_name)
VALUES (1, 'one', 'one');

INSERT INTO schedule_templates (id, week_parity, day, period, auditorium_id,
                                course_id, group_id, professor_id)
VALUES (1, false, 'MONDAY', 'FIRST', 1, 1, 1, 1);
INSERT INTO schedule_templates (id, week_parity, day, period, auditorium_id,
                                course_id, group_id, professor_id)
VALUES (2, false, 'TUESDAY', 'FIRST', 1, 1, 1, 1);
INSERT INTO schedule_templates (id, week_parity, day, period, auditorium_id,
                                course_id, group_id, professor_id)
VALUES (3, true, 'MONDAY', 'FIRST', 1, 1, 1, 1);