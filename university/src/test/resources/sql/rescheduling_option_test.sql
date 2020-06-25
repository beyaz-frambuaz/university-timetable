--saveAll()
INSERT INTO auditoriums (id, name)
VALUES (1, 'one');

INSERT INTO rescheduling_options (id, day, period, auditorium_id)
VALUES (1, 'MONDAY', 'FIRST', 1);
INSERT INTO rescheduling_options (id, day, period, auditorium_id)
VALUES (2, 'MONDAY', 'SECOND', 1);
INSERT INTO rescheduling_options (id, day, period, auditorium_id)
VALUES (3, 'TUESDAY', 'FIRST', 1);
