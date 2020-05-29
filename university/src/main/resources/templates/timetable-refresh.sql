DROP TABLE IF EXISTS rescheduling_options CASCADE;
DROP TABLE IF EXISTS schedules CASCADE;
DROP TABLE IF EXISTS schedule_templates CASCADE;

CREATE TABLE schedule_templates
(
    id            BIGSERIAL PRIMARY KEY,
    week_parity   BOOLEAN,
    day           ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'),
    period        ENUM('FIRST', 'SECOND', 'THIRD', 'FOURTH', 'FIFTH'),
    auditorium_id BIGINT REFERENCES auditoriums (id) ON UPDATE CASCADE ON DELETE CASCADE,
    course_id     BIGINT REFERENCES courses (id) ON UPDATE CASCADE ON DELETE CASCADE,
    group_id      BIGINT REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE,
    professor_id  BIGINT REFERENCES professors (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE schedules
(
    id            BIGSERIAL PRIMARY KEY,
    template_id   BIGINT REFERENCES schedule_templates (id) ON UPDATE CASCADE ON DELETE CASCADE,
    on_date       DATE,
    day           ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'),
    period        ENUM('FIRST', 'SECOND', 'THIRD', 'FOURTH', 'FIFTH'),
    auditorium_id BIGINT REFERENCES auditoriums (id) ON UPDATE CASCADE ON DELETE CASCADE,
    course_id     BIGINT REFERENCES courses (id) ON UPDATE CASCADE ON DELETE CASCADE,
    group_id      BIGINT REFERENCES groups (id) ON UPDATE CASCADE ON DELETE CASCADE,
    professor_id  BIGINT REFERENCES professors (id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE rescheduling_options
(
    id            BIGSERIAL PRIMARY KEY,
    day           ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'),
    period        ENUM('FIRST', 'SECOND', 'THIRD', 'FOURTH', 'FIFTH'),
    auditorium_id BIGINT REFERENCES auditoriums (id) ON UPDATE CASCADE ON DELETE CASCADE
);
