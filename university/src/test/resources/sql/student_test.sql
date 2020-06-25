--saveAll()
INSERT INTO groups (id, name)
VALUES (1, 'one');
INSERT INTO groups (id, name)
VALUES (2, 'two');
INSERT INTO groups (id, name)
VALUES (3, 'three');

INSERT INTO students (id, first_name, last_name, group_id)
VALUES (1, 'one', 'one', 1);
INSERT INTO students (id, first_name, last_name, group_id)
VALUES (2, 'two', 'two', 2);
INSERT INTO students (id, first_name, last_name, group_id)
VALUES (3, 'three', 'three', 3);
