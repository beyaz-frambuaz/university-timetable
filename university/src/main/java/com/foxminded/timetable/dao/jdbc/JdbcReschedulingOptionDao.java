package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcReschedulingOptionDao implements ReschedulingOptionDao {

    private static final String FIND_ALL_SQL = "SELECT rescheduling_options.id,"
            + " rescheduling_options.day, rescheduling_options.period, "
            + "rescheduling_options.auditorium_id, auditoriums.name "
            + "FROM rescheduling_options LEFT JOIN auditoriums "
            + "ON rescheduling_options.auditorium_id = auditoriums.id";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {

        log.debug("Counting rescheduling options");
        String sql = "SELECT COUNT(*) FROM rescheduling_options";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<ReschedulingOption> findAll() {

        log.debug("Retrieving rescheduling options");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public List<ReschedulingOption> findDayReschedulingOptionsForSchedule(
            boolean weekParity, LocalDate date, Schedule schedule) {

        log.debug("Retrieving rescheduling options for {} for schedule id: {}",
                date, schedule.getId());
        String filter = " WHERE rescheduling_options.day = :day AND "
                + "(rescheduling_options.period, "
                + "rescheduling_options.auditorium_id) "
                + "NOT IN ( (SELECT schedule_templates.period, "
                + "schedule_templates.auditorium_id FROM schedule_templates "
                + "WHERE schedule_templates.week_parity = :weekParity "
                + "AND schedule_templates.day = :day ) UNION "
                + "( SELECT schedules.period, schedules.auditorium_id "
                + "FROM schedules WHERE schedules.on_date = :date ) ) "
                + "AND rescheduling_options.period NOT IN "
                + "( (SELECT schedule_templates.period FROM schedule_templates "
                + "WHERE schedule_templates.week_parity = :weekParity "
                + "AND schedule_templates.day = :day "
                + "AND (schedule_templates.group_id = :groupId "
                + "OR schedule_templates.professor_id = :professorId ) ) "
                + "UNION ( SELECT schedules.period FROM schedules "
                + "WHERE schedules.on_date = :date "
                + "AND (schedules.group_id = :groupId "
                + "OR schedules.professor_id = :professorId) ) )";
        SqlParameterSource paramSource = new MapSqlParameterSource().addValue(
                "day", date.getDayOfWeek().toString())
                .addValue("weekParity", weekParity)
                .addValue("date", date.toString())
                .addValue("groupId", schedule.getGroup().getId())
                .addValue("professorId", schedule.getProfessor().getId());

        return jdbc.query(FIND_ALL_SQL + filter, paramSource, this::mapRow);
    }

    @Override
    public Optional<ReschedulingOption> findById(long id) {

        log.debug("Looking for rescheduling option by ID {}", id);
        try {
            String filter = " WHERE rescheduling_options.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.ofNullable(
                    jdbc.queryForObject(FIND_ALL_SQL + filter, paramSource,
                            this::mapRow));

        } catch (EmptyResultDataAccessException e) {
            log.warn("No rescheduling option found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<ReschedulingOption> saveAll(List<ReschedulingOption> options) {

        String sql = "INSERT INTO rescheduling_options (day, period, "
                + "auditorium_id) VALUES (:day, :period, :auditoriumId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (ReschedulingOption option : options) {
            paramSource.add(new MapSqlParameterSource().addValue("day",
                    option.getDay().toString())
                    .addValue("period", option.getPeriod().name())
                    .addValue("auditoriumId", option.getAuditorium().getId()));
        }
        jdbc.batchUpdate(sql, paramSource.toArray(
                new SqlParameterSource[paramSource.size()]));
        log.debug("Rescheduling options saved");

        return options;
    }

    private ReschedulingOption mapRow(ResultSet rs, int rowNumber)
            throws SQLException {

        return new ReschedulingOption(rs.getLong(1),
                DayOfWeek.valueOf(rs.getString(2)),
                Period.valueOf(rs.getString(3)),
                new Auditorium(rs.getLong(4), rs.getString(5)));
    }

}
