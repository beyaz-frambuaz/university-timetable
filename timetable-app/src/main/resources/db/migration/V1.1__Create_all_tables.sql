CREATE TABLE auditoriums
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE courses
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE groups
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(255)
);

CREATE TABLE professors
(
    id         BIGINT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255)
);

CREATE TABLE professors_courses
(
    professor_id BIGINT REFERENCES professors (id) ON UPDATE CASCADE ON DELETE CASCADE,
    courses_id   BIGINT REFERENCES courses (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE students
(
    id         BIGINT PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),
    group_id   BIGINT REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TYPE DAY_OF_WEEK AS ENUM ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY');
CREATE TYPE PERIOD_ORDER AS ENUM ('FIRST', 'SECOND', 'THIRD', 'FOURTH', 'FIFTH');

CREATE TABLE schedule_templates
(
    id            BIGINT PRIMARY KEY,
    week_parity   BOOLEAN,
    day           DAY_OF_WEEK,
    period        PERIOD_ORDER,
    auditorium_id BIGINT REFERENCES auditoriums (id) ON UPDATE CASCADE ON DELETE CASCADE,
    course_id     BIGINT REFERENCES courses (id) ON UPDATE CASCADE ON DELETE CASCADE,
    group_id      BIGINT REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE,
    professor_id  BIGINT REFERENCES professors (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE schedules
(
    id            BIGINT PRIMARY KEY,
    template_id   BIGINT REFERENCES schedule_templates (id) ON UPDATE CASCADE ON DELETE CASCADE,
    on_date       DATE,
    day           DAY_OF_WEEK,
    period        PERIOD_ORDER,
    auditorium_id BIGINT REFERENCES auditoriums (id) ON UPDATE CASCADE ON DELETE CASCADE,
    course_id     BIGINT REFERENCES courses (id) ON UPDATE CASCADE ON DELETE CASCADE,
    group_id      BIGINT REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE,
    professor_id  BIGINT REFERENCES professors (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE rescheduling_options
(
    id            BIGINT PRIMARY KEY,
    day           DAY_OF_WEEK,
    period        PERIOD_ORDER,
    auditorium_id BIGINT REFERENCES auditoriums (id) ON UPDATE CASCADE ON DELETE CASCADE
);
