package com.foxminded.timetable.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcStudentDao implements StudentDao {

    private static final String FIND_ALL_SQL = "SELECT students.id, "
            + "students.first_name, students.last_name, groups.id, groups.name " 
            + "FROM students LEFT JOIN groups ON students.group_id = groups.id";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {
        
        log.debug("Counting students");
        String sql = "SELECT COUNT(*) FROM students";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<Student> findAll() {

        log.debug("Retrieving students");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public List<Student> findAllByGroups(List<Group> groups) {

        log.debug("Retrieving students in groups");
        String filter = " WHERE students.group_id = :groupId";
        List<Student> students = new ArrayList<>();
        for (Group group : groups) {
            SqlParameterSource paramSource = new MapSqlParameterSource(
                    "groupId", group.getId());
            students.addAll(jdbc.query(FIND_ALL_SQL + filter, paramSource,
                    this::mapRow));
        }

        return students;
    }

    @Override
    public Optional<Student> findById(long id) {
        
        try {
            String filter = " WHERE students.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.of(jdbc.queryForObject(FIND_ALL_SQL + filter,
                    paramSource, this::mapRow));
        } catch (EmptyResultDataAccessException e) {
            log.warn("No student found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Student> saveAll(List<Student> students) {
        
        String sql = "INSERT INTO students (first_name, last_name, group_id) "
                + "VALUES (:firstName, :lastName, :groupId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (Student student : students) {
            paramSource.add(new MapSqlParameterSource()
                    .addValue("firstName", student.getFirstName())
                    .addValue("lastName", student.getLastName())
                    .addValue("groupId", student.getGroup().getId()));
        }
        jdbc.batchUpdate(sql, paramSource
                .toArray(new SqlParameterSource[paramSource.size()]));
        log.debug("Students saved");
        
        return students;
    }

    private Student mapRow(ResultSet rs, int rowNumber) throws SQLException {

        return new Student(rs.getLong(1), rs.getString(2), rs.getString(3),
                new Group(rs.getLong(4), rs.getString(5)));
    }

}
