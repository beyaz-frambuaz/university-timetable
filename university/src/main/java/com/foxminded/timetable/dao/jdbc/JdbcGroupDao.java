package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.model.Group;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcGroupDao implements GroupDao {

    private static final String INSERT_SQL   =
            "INSERT INTO groups (name) " + "VALUES (:name)";
    private static final String FIND_ALL_SQL =
            "SELECT groups.id, groups.name" + " FROM groups";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {

        log.debug("Counting groups");
        String sql = "SELECT COUNT(*) FROM groups";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<Group> findAll() {

        log.debug("Retrieving groups");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public List<Group> findAllByProfessorAndCourse(long professorId,
            long courseId) {

        log.debug("Retrieving groups by professor (ID {}) and course (ID {})",
                professorId, courseId);
        String sql = "SELECT schedule_templates.group_id, groups.name FROM "
                + "schedule_templates RIGHT JOIN groups ON schedule_templates"
                + ".group_id = groups.id WHERE schedule_templates"
                + ".professor_id = :professorId AND schedule_templates"
                + ".course_id = :courseId";
        SqlParameterSource paramSource = new MapSqlParameterSource().addValue(
                "professorId", professorId).addValue("courseId", courseId);

        return jdbc.query(sql, paramSource, this::mapRow);
    }

    @Override
    public Optional<Group> findById(long id) {

        log.debug("Looking for group by ID {}", id);
        try {
            String filter = " WHERE groups.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.ofNullable(
                    jdbc.queryForObject(FIND_ALL_SQL + filter, paramSource,
                            this::mapRow));

        } catch (EmptyResultDataAccessException e) {
            log.warn("No group found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public Group save(Group group) {

        jdbc.update(INSERT_SQL,
                new MapSqlParameterSource("name", group.getName()));
        log.debug("Saved {}", group);

        return group;
    }

    @Override
    public List<Group> saveAll(List<Group> groups) {

        jdbc.batchUpdate(INSERT_SQL,
                SqlParameterSourceUtils.createBatch(groups));
        log.debug("Groups saved");

        return groups;
    }

    @Override
    public Group update(Group group) {

        String sql = "UPDATE groups SET groups.name = :name "
                + "WHERE groups.id = :id";
        jdbc.update(sql,
                new MapSqlParameterSource().addValue("name", group.getName())
                        .addValue("id", group.getId()));
        log.debug("Updated {}", group);

        return group;
    }

    private Group mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new Group(rs.getLong(1), rs.getString(2));
    }

}
