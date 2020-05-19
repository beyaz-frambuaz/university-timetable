package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcScheduleTemplateDao implements ScheduleTemplateDao {

    private static final String INSERT_SQL   =
            "INSERT INTO schedule_templates (week_parity, day, period, "
                    + "auditorium_id, course_id, group_id, professor_id) "
                    + "VALUES (:weekParity, :day, :period, :auditoriumId, "
                    + ":courseId, :groupId, :professorId)";
    private static final String FIND_ALL_SQL =
            "SELECT schedule_templates.id, schedule_templates.week_parity, "
                    + "schedule_templates.day, schedule_templates.period, "
                    + "schedule_templates.auditorium_id, auditoriums.name, "
                    + "schedule_templates.course_id, courses.name, "
                    + "schedule_templates.group_id, groups.name, "
                    + "schedule_templates.professor_id, professors"
                    + ".first_name, professors.last_name "
                    + "FROM schedule_templates LEFT JOIN auditoriums ON "
                    + "schedule_templates.auditorium_id = auditoriums.id LEFT"
                    + " JOIN courses ON schedule_templates.course_id = "
                    + "courses.id LEFT JOIN groups ON schedule_templates"
                    + ".group_id = groups.id LEFT JOIN professors ON "
                    + "schedule_templates.professor_id = professors.id";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {

        log.debug("Counting schedule templates");
        String sql = "SELECT COUNT(*) FROM schedule_templates";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<ScheduleTemplate> findAll() {

        log.debug("Retrieving schedule templates");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public List<ScheduleTemplate> findAllByDate(boolean weekParity,
            DayOfWeek day) {

        log.debug("Retrieving schedule templates for week parity {} on {}",
                weekParity, day);
        String filter = " WHERE schedule_templates.week_parity = :weekParity "
                + "AND schedule_templates.day = :day";
        SqlParameterSource parameters = new MapSqlParameterSource().addValue(
                "weekParity", weekParity).addValue("day", day.name());
        return jdbc.query(FIND_ALL_SQL + filter, parameters, this::mapRow);
    }

    @Override
    public Optional<ScheduleTemplate> findById(long id) {

        log.debug("Looking for schedule template by ID {}", id);
        try {
            String filter = " WHERE schedule_templates.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.ofNullable(
                    jdbc.queryForObject(FIND_ALL_SQL + filter, paramSource,
                            this::mapRow));

        } catch (EmptyResultDataAccessException e) {
            log.warn("No schedule template found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public ScheduleTemplate save(ScheduleTemplate template) {

        SqlParameterSource paramSource = new MapSqlParameterSource().addValue(
                "weekParity", template.getWeekParity())
                .addValue("day", template.getDay().toString())
                .addValue("period", template.getPeriod().name())
                .addValue("auditoriumId", template.getAuditorium().getId())
                .addValue("courseId", template.getCourse().getId())
                .addValue("groupId", template.getGroup().getId())
                .addValue("professorId", template.getProfessor().getId());
        jdbc.update(INSERT_SQL, paramSource);
        log.debug("Schedule templates saved");

        return template;
    }

    @Override
    public List<ScheduleTemplate> saveAll(List<ScheduleTemplate> templates) {

        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (ScheduleTemplate template : templates) {
            paramSource.add(new MapSqlParameterSource().addValue("weekParity",
                    template.getWeekParity())
                    .addValue("day", template.getDay().toString())
                    .addValue("period", template.getPeriod().name())
                    .addValue("auditoriumId", template.getAuditorium().getId())
                    .addValue("courseId", template.getCourse().getId())
                    .addValue("groupId", template.getGroup().getId())
                    .addValue("professorId", template.getProfessor().getId()));
        }
        jdbc.batchUpdate(INSERT_SQL, paramSource.toArray(
                new SqlParameterSource[paramSource.size()]));
        log.debug("Schedule templates saved");
        return templates;
    }

    @Override
    public ScheduleTemplate update(ScheduleTemplate template) {

        String update = "UPDATE schedule_templates "
                + "SET schedule_templates.week_parity = :weekParity, "
                + "schedule_templates.day = :day, "
                + "schedule_templates.period = :period, "
                + "schedule_templates.auditorium_id = :auditoriumId "
                + "WHERE schedule_templates.id = :templateId";
        SqlParameterSource paramSource = new MapSqlParameterSource().addValue(
                "weekParity", template.getWeekParity())
                .addValue("day", template.getDay().toString())
                .addValue("period", template.getPeriod().name())
                .addValue("auditoriumId", template.getAuditorium().getId())
                .addValue("templateId", template.getId());
        jdbc.update(update, paramSource);
        log.debug("Updated schedule template with ID {}", template.getId());

        return template;
    }

    private ScheduleTemplate mapRow(ResultSet rs, int rowNumber)
            throws SQLException {

        return new ScheduleTemplate(rs.getLong(1), rs.getBoolean(2),
                DayOfWeek.valueOf(rs.getString(3)),
                Period.valueOf(rs.getString(4)),
                new Auditorium(rs.getLong(5), rs.getString(6)),
                new Course(rs.getLong(7), rs.getString(8)),
                new Group(rs.getLong(9), rs.getString(10)),
                new Professor(rs.getLong(11), rs.getString(12),
                        rs.getString(13)));
    }

}
