package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JdbcAuditoriumDao implements AuditoriumDao {

    private static final String FIND_ALL_SQL = "SELECT auditoriums.id, "
            + "auditoriums.name FROM auditoriums";
    private static final String INSERT_SQL   = "INSERT INTO auditoriums "
            + "(name) VALUES (:name)";

    private final NamedParameterJdbcTemplate jdbc;

    @Override
    public long count() {

        log.debug("Counting auditoriums");
        String sql = "SELECT COUNT(*) FROM auditoriums";
        return jdbc.getJdbcOperations().queryForObject(sql, Long.class);
    }

    @Override
    public List<Auditorium> findAll() {

        log.debug("Retrieving auditoriums");
        return jdbc.query(FIND_ALL_SQL, this::mapRow);
    }

    @Override
    public List<Auditorium> findAllAvailable(LocalDate date,
            Period period) {

        log.debug("Retrieving available auditoriums for {} on {}", period,
                date);
        String filter = " WHERE auditoriums.id NOT IN (SELECT schedules.auditorium_id "
                + "FROM schedules WHERE schedules.on_date = :date AND "
                + "schedules.period = :period);";
        SqlParameterSource paramSource = new MapSqlParameterSource()
                .addValue("date", date.toString())
                .addValue("period", period.name());
        List<Auditorium> auditoriums = jdbc.query(FIND_ALL_SQL + filter, paramSource,
                this::mapRow);
        log.debug("Found available auditoriums: {}", auditoriums);
        return auditoriums;
    }

    @Override
    public Optional<Auditorium> findById(long id) {

        log.debug("Looking for auditorium by ID {}", id);
        try {
            String filter = " WHERE auditoriums.id = :id";
            SqlParameterSource paramSource = new MapSqlParameterSource("id",
                    id);

            return Optional.ofNullable(

                    jdbc.queryForObject(FIND_ALL_SQL + filter, paramSource,
                            this::mapRow));

        } catch (EmptyResultDataAccessException e) {
            log.warn("No auditorium found with ID {}", id);
            return Optional.empty();
        }
    }

    @Override
    public Auditorium save(Auditorium newAuditorium) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(INSERT_SQL,
                new MapSqlParameterSource("name", newAuditorium.getName()),
                keyHolder);
        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        newAuditorium.setId(id);
        log.debug("Saved {}", newAuditorium);

        return newAuditorium;
    }

    @Override
    public List<Auditorium> saveAll(List<Auditorium> auditoriums) {

        jdbc.batchUpdate(INSERT_SQL,
                SqlParameterSourceUtils.createBatch(auditoriums));
        log.debug("Auditoriums saved");

        return auditoriums;
    }

    @Override
    public Auditorium update(Auditorium auditorium) {

        String sql = "UPDATE auditoriums SET auditoriums.name = :name WHERE "
                + "auditoriums.id = :id";
        jdbc.update(sql, new MapSqlParameterSource().addValue("name",
                auditorium.getName()).addValue("id", auditorium.getId()));
        log.debug("Updated {}", auditorium);

        return auditorium;
    }

    private Auditorium mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new Auditorium(rs.getLong(1), rs.getString(2));
    }

}