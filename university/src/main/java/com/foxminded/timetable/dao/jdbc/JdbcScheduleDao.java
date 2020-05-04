package com.foxminded.timetable.dao.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.Schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcScheduleDao implements ScheduleDao {

    private static final String FIND_ALL_SQL = "SELECT schedules.id, "
            + "schedules.template_id, schedules.on_date, schedules.day, "
            + "schedules.period, schedules.auditorium_id, auditoriums.name, "
            + "schedules.course_id, courses.name, schedules.group_id, groups.name, "
            + "schedules.professor_id, professors.first_name, professors.last_name "
            + "FROM schedules "
            + "LEFT JOIN auditoriums ON schedules.auditorium_id = auditoriums.id "
            + "LEFT JOIN courses ON schedules.course_id = courses.id "
            + "LEFT JOIN groups ON schedules.group_id = groups.id "
            + "LEFT JOIN professors ON schedules.professor_id = professors.id";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {

        log.debug("Counting schedules");
        String sql = "SELECT COUNT(*) FROM schedules";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<Schedule> findAll() {

        log.debug("Retrieving schedules");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public List<Schedule> findAllByDate(LocalDate date) {

        log.debug("Retrieving schedules for {}", date);
        String filter = " WHERE schedules.on_date = :date";
        SqlParameterSource parameters = new MapSqlParameterSource("date",
                date.toString());
        return jdbc.query(FIND_ALL_SQL + filter, parameters, this::mapRow);
    }

    @Override
    public List<Schedule> findAllByTemplateId(long templateId) {

        log.debug("Retrieving schedules by template ID {}", templateId);
        String filter = " WHERE schedules.template_id = :templateId";
        SqlParameterSource parameters = new MapSqlParameterSource("templateId",
                templateId);
        return jdbc.query(FIND_ALL_SQL + filter, parameters, this::mapRow);
    }

    @Override
    public Optional<Schedule> findById(long id) {

        log.debug("Looking for schedule by ID {}", id);
        try {
            String filter = " WHERE schedules.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.of(jdbc.queryForObject(FIND_ALL_SQL + filter,
                    paramSource, this::mapRow));

        } catch (EmptyResultDataAccessException e) {
            log.warn("No schedule found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public Schedule save(Schedule schedule) {

        String sql = "INSERT INTO schedules (template_id, on_date, day, "
                + "period, auditorium_id, course_id, group_id, professor_id) "
                + "VALUES (:templateId, :date, :day, :period, :auditoriumId, "
                + ":courseId, :groupId, :professorId)";
        SqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("templateId", schedule.getTemplateId())
                .addValue("date", schedule.getDate().toString())
                .addValue("day", schedule.getDay().toString())
                .addValue("period", schedule.getPeriod().name())
                .addValue("auditoriumId", schedule.getAuditorium().getId())
                .addValue("courseId", schedule.getCourse().getId())
                .addValue("groupId", schedule.getGroup().getId())
                .addValue("professorId", schedule.getProfessor().getId());
        jdbc.update(sql, paramSource);
        log.debug("Schedule saved");

        return schedule;

    }

    @Override
    public List<Schedule> saveAll(List<Schedule> schedules) {

        String sql = "INSERT INTO schedules (template_id, on_date, day, "
                + "period, auditorium_id, course_id, group_id, professor_id) "
                + "VALUES (:templateId, :date, :day, :period, :auditoriumId, "
                + ":courseId, :groupId, :professorId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (Schedule schedule : schedules) {
            paramSource.add(new MapSqlParameterSource()
                    .addValue("templateId", schedule.getTemplateId())
                    .addValue("date", schedule.getDate().toString())
                    .addValue("day", schedule.getDay().toString())
                    .addValue("period", schedule.getPeriod().name())
                    .addValue("auditoriumId", schedule.getAuditorium().getId())
                    .addValue("courseId", schedule.getCourse().getId())
                    .addValue("groupId", schedule.getGroup().getId())
                    .addValue("professorId", schedule.getProfessor().getId()));
        }
        jdbc.batchUpdate(sql, paramSource
                .toArray(new SqlParameterSource[paramSource.size()]));
        log.debug("Schedules saved");

        return schedules;
    }

    @Override
    public Schedule update(Schedule schedule) {

        String update = "UPDATE schedules SET schedules.on_date = :date, "
                + "schedules.day = :day, schedules.period = :period, "
                + "schedules.auditorium_id = :auditoriumId, "
                + "schedules.course_id = :courseId, "
                + "schedules.group_id = :groupId, "
                + "schedules.professor_id = :professorId "
                + "WHERE schedules.id = :scheduleId";
        SqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("date", schedule.getDate().toString())
                .addValue("day", schedule.getDay().toString())
                .addValue("period", schedule.getPeriod().name())
                .addValue("auditoriumId", schedule.getAuditorium().getId())
                .addValue("courseId", schedule.getCourse().getId())
                .addValue("groupId", schedule.getGroup().getId())
                .addValue("professorId", schedule.getProfessor().getId())
                .addValue("scheduleId", schedule.getId());
        jdbc.update(update, paramSource);
        log.debug("Updated schedule ID {}", schedule.getId());

        return schedule;
    }

    @Override
    public void updateAllWithTemplateId(Schedule schedule, int deltaDays) {

        String update = "UPDATE schedules "
                + "SET schedules.on_date = schedules.on_date + CAST(:deltaDays AS INTEGER), "
                + "schedules.day = :day, schedules.period = :period, "
                + "schedules.auditorium_id = :auditoriumId "
                + "WHERE schedules.template_id = :templateId";
        SqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("deltaDays", deltaDays)
                .addValue("day", schedule.getDay().toString())
                .addValue("period", schedule.getPeriod().name())
                .addValue("auditoriumId", schedule.getAuditorium().getId())
                .addValue("templateId", schedule.getTemplateId());
        int rows = jdbc.update(update, paramSource);
        log.debug("Rescheduled {} schedules linked to template ID {}", rows,
                schedule.getTemplateId());
    }

    private Schedule mapRow(ResultSet rs, int rowNumber) throws SQLException {

        return new Schedule(rs.getLong(1), rs.getLong(2),
                rs.getDate(3).toLocalDate(), DayOfWeek.valueOf(rs.getString(4)),
                Period.valueOf(rs.getString(5)),
                new Auditorium(rs.getLong(6), rs.getString(7)),
                new Course(rs.getLong(8), rs.getString(9)),
                new Group(rs.getLong(10), rs.getString(11)), new Professor(
                        rs.getLong(12), rs.getString(13), rs.getString(14)));
    }

}
