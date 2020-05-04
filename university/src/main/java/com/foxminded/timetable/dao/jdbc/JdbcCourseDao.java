package com.foxminded.timetable.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcCourseDao implements CourseDao {

    private static final String INSERT_SQL = "INSERT INTO courses (name) "
            + "VALUES (:name)";
    private static final String FIND_ALL_SQL = "SELECT courses.id, courses.name"
            + " FROM courses";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {

        log.debug("Counting courses");
        String sql = "SELECT COUNT(*) FROM courses";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<Course> findAll() {

        log.debug("Retrieving courses");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public Optional<Course> findById(long id) {

        log.debug("Looking for course by ID {}", id);
        try {
            String filter = " WHERE courses.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.of(jdbc.queryForObject(FIND_ALL_SQL + filter,
                    paramSource, this::mapRow));

        } catch (EmptyResultDataAccessException e) {
            log.warn("No course found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public Course save(Course course) {

        jdbc.update(INSERT_SQL,
                new MapSqlParameterSource("name", course.getName()));
        log.debug("Saved {}", course);

        return course;
    }

    @Override
    public List<Course> saveAll(List<Course> courses) {

        jdbc.batchUpdate(INSERT_SQL,
                SqlParameterSourceUtils.createBatch(courses));
        log.debug("Courses saved");

        return courses;
    }

    @Override
    public Course update(Course course) {

        String sql = "UPDATE courses SET courses.name = :name WHERE "
                + "courses.id = :id";
        jdbc.update(sql,
                new MapSqlParameterSource().addValue("name", course.getName())
                        .addValue("id", course.getId()));
        log.debug("Updated {}", course);

        return course;
    }

    private Course mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Course(rs.getLong(1), rs.getString(2));
    }

}
