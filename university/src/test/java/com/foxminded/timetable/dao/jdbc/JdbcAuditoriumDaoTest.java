package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcAuditoriumDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private AuditoriumDao auditoriumRepository;

    private List<Auditorium> auditoriums = Arrays.asList(
            new Auditorium(1L, "one"), new Auditorium(2L, "two"),
            new Auditorium(3L, "three"));

    @BeforeEach
    private void setUp() {
        this.auditoriumRepository = new JdbcAuditoriumDao(jdbc);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void countShouldReturnCorrectAmountOfAuditoriums() {

        long expected = 3L;
        long actual = auditoriumRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void findAllShouldRetrieveCorrectListOfAuditoriums() {

        List<Auditorium> actual = auditoriumRepository.findAll();

        assertThat(actual).hasSameElementsAs(auditoriums);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void findAllAvailableShouldRetrieveCorrectListOfAvailableAuditoriums() {

        Auditorium freeAuditoriumOne = auditoriums.get(0);
        Auditorium freeAuditoriumTwo = auditoriums.get(1);
        Auditorium busyAuditorium = auditoriums.get(2);

        List<Auditorium> actual = auditoriumRepository.findAllAvailable(
                false, LocalDate.of(2020, 9, 7), Period.SECOND);

        assertThat(actual).containsOnly(freeAuditoriumOne, freeAuditoriumTwo)
                .doesNotContain(busyAuditorium);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void findByIdShouldReturnCorrectAuditorium() {

        Auditorium expectedAuditorium = new Auditorium(3L, "three");
        Optional<Auditorium> actualAuditorium = auditoriumRepository
                .findById(3L);

        assertThat(actualAuditorium).isNotEmpty().contains(expectedAuditorium);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Auditorium> actualAuditorium = auditoriumRepository
                .findById(999L);

        assertThat(actualAuditorium).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllAuditoriums() {

        auditoriumRepository.saveAll(auditoriums);

        String sql = "SELECT auditoriums.id, auditoriums.name FROM auditoriums";
        List<Auditorium> actual = jdbc.query(sql, (ResultSet rs,
                int rowNum) -> new Auditorium(rs.getLong(1), rs.getString(2)));

        assertThat(actual).hasSameElementsAs(auditoriums);
    }

}
