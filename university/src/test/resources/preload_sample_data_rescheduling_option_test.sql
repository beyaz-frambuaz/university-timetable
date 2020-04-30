--saveAll()
INSERT INTO auditoriums (name) VALUES ('one');
INSERT INTO auditoriums (name) VALUES ('two');

INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'FIRST', 1);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'FIRST', 2);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'SECOND', 1);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'SECOND', 2);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'THIRD', 1);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'THIRD', 2);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'FOURTH', 1);
INSERT INTO rescheduling_options (day, period, auditorium_id) 
VALUES ('MONDAY', 'FOURTH', 2);

--save more data for findDayReschedulingOptionsForSchedule()
INSERT INTO courses (name) VALUES ('one');
INSERT INTO groups (name) VALUES ('one');
INSERT INTO groups (name) VALUES ('two');
INSERT INTO professors (first_name, last_name) VALUES ('one', 'one');
INSERT INTO professors (first_name, last_name) VALUES ('two', 'two');

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'FIRST', 1, 1, 1, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'SECOND', 1, 1, 2, 1);

INSERT INTO schedule_templates (week_parity, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (false, 'MONDAY', 'THIRD', 1, 1, 1, 2);

INSERT INTO schedules (template_id, on_date, day, period, auditorium_id, 
course_id, group_id, professor_id) VALUES (1, '2020-09-07', 'MONDAY', 'FIRST', 1, 1, 1, 1);
