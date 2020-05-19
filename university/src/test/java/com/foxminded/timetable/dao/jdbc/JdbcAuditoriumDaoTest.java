package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcAuditoriumDaoTest {

    private final Auditorium                 auditoriumOne   = new Auditorium(
            1L, "one");
    private final Auditorium                 auditoriumTwo   = new Auditorium(
            2L, "two");
    private final Auditorium                 auditoriumThree = new Auditorium(
            3L, "three");
    private final List<Auditorium>           auditoriums     = Arrays.asList(
            auditoriumOne, auditoriumTwo, auditoriumThree);
    @Autowired
    private       NamedParameterJdbcTemplate jdbc;
    private       AuditoriumDao              auditoriumRepository;

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

        List<Auditorium> actual = auditoriumRepository.findAllAvailable(false,
                LocalDate.of(2020, 9, 7), Period.SECOND);

        assertThat(actual).containsOnly(auditoriumOne, auditoriumTwo)
                .doesNotContain(auditoriumThree);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void findByIdShouldReturnCorrectAuditorium() {

        Auditorium expectedAuditorium = new Auditorium(3L, "three");
        Optional<Auditorium> actualAuditorium = auditoriumRepository.findById(
                3L);

        assertThat(actualAuditorium).isNotEmpty().contains(expectedAuditorium);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Auditorium> actualAuditorium = auditoriumRepository.findById(
                999L);

        assertThat(actualAuditorium).isEmpty();
    }

    @Test
    public void saveShouldAddAuditorium() {

        Auditorium expected = auditoriumRepository.save(auditoriumOne);

        String sql = "SELECT auditoriums.id, auditoriums.name FROM auditoriums "
                + "WHERE auditoriums.id = :id";
        Auditorium actual = jdbc.queryForObject(sql,
                new MapSqlParameterSource("id", auditoriumOne.getId()),
                (ResultSet rs, int rowNum) -> new Auditorium(rs.getLong(1),
                        rs.getString(2)));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_auditorium_test.sql")
    public void udpateShouldUpdateAuditoriumName() {

        String newName = "new name";
        Auditorium expected = new Auditorium(auditoriumOne.getId(),
                auditoriumOne.getName());
        expected.setName(newName);

        auditoriumRepository.update(expected);

        String sql = "SELECT auditoriums.id, auditoriums.name FROM auditoriums "
                + "WHERE auditoriums.id = :id";
        Optional<Auditorium> actual = Optional.of(jdbc.queryForObject(sql,
                new MapSqlParameterSource("id", auditoriumOne.getId()),
                (ResultSet rs, int rowNum) -> new Auditorium(rs.getLong(1),
                        rs.getString(2))));

        assertThat(actual).isPresent().contains(expected);
    }

    @Test
    public void saveAllShouldSaveAllAuditoriums() {

        auditoriumRepository.saveAll(auditoriums);

        String sql = "SELECT auditoriums.id, auditoriums.name FROM auditoriums";
        List<Auditorium> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new Auditorium(rs.getLong(1),
                        rs.getString(2)));

        assertThat(actual).hasSameElementsAs(auditoriums);
    }

}
