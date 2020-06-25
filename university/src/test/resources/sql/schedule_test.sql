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

INSERT INTO schedules (id, template_id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (1, 1, '2020-06-01', 'MONDAY', 'FIRST', 1, 1, 1, 1);
INSERT INTO schedules (id, template_id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (2, 2, '2020-06-02', 'TUESDAY', 'FIRST', 1, 1, 1, 1);
INSERT INTO schedules (id, template_id, on_date, day, period, auditorium_id,
                       course_id, group_id, professor_id)
VALUES (3, 1, '2020-06-15', 'MONDAY', 'FIRST', 1, 1, 1, 1);
