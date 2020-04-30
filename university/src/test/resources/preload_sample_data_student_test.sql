--saveAll()
INSERT INTO groups (name) VALUES ('one');
INSERT INTO groups (name) VALUES ('two');

INSERT INTO students (first_name, last_name, group_id) VALUES ('one', 'one', 1);
INSERT INTO students (first_name, last_name, group_id) VALUES ('two', 'two', 1);
INSERT INTO students (first_name, last_name, group_id) VALUES ('three', 'three', 2);
