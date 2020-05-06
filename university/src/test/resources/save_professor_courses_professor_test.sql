--save new course
INSERT INTO courses (name) VALUES ('new');

--save old professor's courses
INSERT INTO professors_courses (professor_id, course_id) VALUES (1, 1);
INSERT INTO professors_courses (professor_id, course_id) VALUES (1, 2);