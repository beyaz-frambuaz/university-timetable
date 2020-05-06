package com.foxminded.timetable.dao.jdbc;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcReschedulingOptionDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private ReschedulingOptionDao optionRepository;

    private LocalDate date = LocalDate.of(2020, 9, 7);
    private Auditorium auditoriumOne = new Auditorium(1L, "one");
    private Auditorium auditoriumTwo = new Auditorium(2L, "two");
    private List<Auditorium> auditoriums = Arrays.asList(auditoriumOne,
            auditoriumTwo);
    private Course course = new Course(1L, "");
    private Group groupOne = new Group(1L, "one");
    private Professor professorOne = new Professor(1L, "one", "one");
    private Schedule schedule = new Schedule(1L, 1L, date, DayOfWeek.MONDAY,
            Period.FIRST, auditoriumOne, course, groupOne, professorOne);

    private List<ReschedulingOption> options;

    @BeforeEach
    private void setUp() {
        this.optionRepository = new JdbcReschedulingOptionDao(jdbc);

        this.options = new ArrayList<>();
        long id = 1L;
        List<Period> periods = Arrays.asList(Period.FIRST, Period.SECOND,
                Period.THIRD, Period.FOURTH);
        for (Period period : periods) {
            for (Auditorium auditorium : auditoriums) {
                options.add(new ReschedulingOption(id, DayOfWeek.MONDAY, period,
                        auditorium));
                id++;
            }
        }
    }

    @Test
    @Sql("classpath:preload_sample_data_rescheduling_option_test.sql")
    public void countShouldReturnCorrectAmountOfOptions() {

        long expected = 8L;
        long actual = optionRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_rescheduling_option_test.sql")
    public void findAllShouldRetrieveCorrectListOfOptions() {

        List<ReschedulingOption> actual = optionRepository.findAll();

        assertThat(actual).hasSameElementsAs(options);
    }

    @Test
    @Sql("classpath:preload_sample_data_rescheduling_option_test.sql")
    public void findDayReschedulingOptionsForScheduleShouldFindNoConflictOptions() {

        List<ReschedulingOption> expected = options.stream()
                .filter(option -> option.getPeriod() == Period.FOURTH)
                .collect(toList());
        List<ReschedulingOption> notExpected = new ArrayList<>(options);
        notExpected.removeAll(expected);

        List<ReschedulingOption> actual = optionRepository
                .findDayReschedulingOptionsForSchedule(false, date, schedule);

        assertThat(actual).containsOnlyElementsOf(expected)
                .doesNotContainAnyElementsOf(notExpected);
    }

    @Test
    @Sql("classpath:preload_sample_data_rescheduling_option_test.sql")
    public void findByIdShouldReturnCorrectOption() {

        ReschedulingOption expectedOption = new ReschedulingOption(1L,
                DayOfWeek.MONDAY, Period.FIRST, auditoriumOne);

        Optional<ReschedulingOption> actualOption = optionRepository
                .findById(expectedOption.getId());

        assertThat(actualOption).isNotEmpty().contains(expectedOption);
    }

    @Test
    @Sql("classpath:preload_sample_data_rescheduling_option_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<ReschedulingOption> actualOption = optionRepository
                .findById(999L);

        assertThat(actualOption).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllOptions() {

        String sqlAuditorium = "INSERT INTO auditoriums (name) VALUES (:name)";
        jdbc.batchUpdate(sqlAuditorium,
                SqlParameterSourceUtils.createBatch(auditoriums));

        optionRepository.saveAll(options);

        String sql = "SELECT rescheduling_options.id,"
                + " rescheduling_options.day, rescheduling_options.period, "
                + "rescheduling_options.auditorium_id, auditoriums.name "
                + "FROM rescheduling_options LEFT JOIN auditoriums "
                + "ON rescheduling_options.auditorium_id = auditoriums.id";
        List<ReschedulingOption> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new ReschedulingOption(
                        rs.getLong(1), DayOfWeek.valueOf(rs.getString(2)),
                        Period.valueOf(rs.getString(3)),
                        new Auditorium(rs.getLong(4), rs.getString(5))));

        assertThat(actual).hasSameElementsAs(options);
    }

}
