package com.foxminded.timetable.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcProfessorDao implements ProfessorDao {

    private static final String FIND_ALL_SQL = "SELECT professors.id, "
            + "professors.first_name, professors.last_name, courses.id, "
            + "courses.name FROM professors LEFT JOIN professors_courses "
            + "ON professors.id = professors_courses.professor_id "
            + "LEFT JOIN courses "
            + "ON courses.id = professors_courses.course_id";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {
        
        log.debug("Counting professors");
        String sql = "SELECT COUNT(*) FROM professors";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<Professor> findAll() {

        log.debug("Retrieving professors");
        return jdbc.query(FIND_ALL_SQL, this::mapResultsToProfessors);
    }

    @Override
    public List<Professor> findAllAvailable(boolean weekParity, LocalDate date,
            Period period) {

        log.debug("Retrieving available professors for {} on {}", period, date);
        String filter = " WHERE professors.id "
                + "NOT IN ( (SELECT schedule_templates.professor_id "
                + "FROM schedule_templates "
                + "WHERE schedule_templates.week_parity = :weekParity "
                + "AND schedule_templates.day = :day "
                + "AND schedule_templates.period = :period) "
                + "UNION (SELECT schedules.professor_id FROM schedules "
                + "WHERE schedules.on_date = :date "
                + "AND schedules.period = :period) );";
        SqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("weekParity", weekParity)
                .addValue("day", date.getDayOfWeek().toString())
                .addValue("period", period.name())
                .addValue("date", date.toString());
        return jdbc.query(FIND_ALL_SQL + filter, paramSource,
                this::mapResultsToProfessors);
    }

    @Override
    public Optional<Professor> findById(long id) {
        
        log.debug("Looking for professor by ID {}", id);
        try {
            String filter = " WHERE professors.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);
            List<Professor> professor = jdbc.query(FIND_ALL_SQL + filter,
                    paramSource, this::mapResultsToProfessors);
            if (professor.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(professor.get(0));
            
        } catch (EmptyResultDataAccessException e) {
            log.warn("No auditorium found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Professor> saveAll(List<Professor> professors) {
        
        String sql = "INSERT INTO professors (first_name, "
                + "last_name) VALUES (:firstName, :lastName)";
        jdbc.batchUpdate(sql, SqlParameterSourceUtils.createBatch(professors));
        log.debug("Professors saved");
        
        return professors;
    }

    @Override
    public void saveAllProfessorsCourses(List<Professor> professors) {
        
        String sql = "INSERT INTO professors_courses (professor_id, course_id)"
                + " VALUES (:professorId, :courseId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (Professor professor : professors) {
            for (Course course : professor.getCourses()) {
                paramSource.add(new MapSqlParameterSource()
                        .addValue("professorId", professor.getId())
                        .addValue("courseId", course.getId()));
            }
        }
        jdbc.batchUpdate(sql, paramSource
                .toArray(new SqlParameterSource[paramSource.size()]));
        log.debug("Course assignments to professors saved");
    }

    private List<Professor> mapResultsToProfessors(ResultSet rs)
            throws SQLException {
        
        List<Professor> professors = new ArrayList<>();
        Professor current = null;
        while (rs.next()) {
            long currentId = rs.getLong(1);
            if (current == null) {
                current = new Professor(currentId, rs.getString(2),
                        rs.getString(3));
            } else if (current.getId() != currentId) {
                professors.add(current);
                current = new Professor(currentId, rs.getString(2),
                        rs.getString(3));
            }
            Long courseId = rs.getLong(4);
            String courseName = rs.getString(5);
            if (courseId != null && courseName != null) {
                current.addCourse(new Course(courseId, courseName));
            }
        }
        if (current != null) {
            professors.add(current);
        }

        return professors;
    }

}
