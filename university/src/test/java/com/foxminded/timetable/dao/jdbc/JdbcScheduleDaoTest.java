package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.Schedule;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcScheduleDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private ScheduleDao scheduleRepository;

    private LocalDate dateOne = LocalDate.of(2020, 9, 7);
    private LocalDate dateTwo = LocalDate.of(2020, 9, 14);

    private Auditorium auditorium = new Auditorium(1L, "one");
    private Auditorium auditoriumNew = new Auditorium(2L, "new");
    private Course course = new Course(1L, "one");
    private Group group = new Group(1L, "one");
    private Professor professor = new Professor(1L, "one", "one");

    private Schedule scheduleOne = new Schedule(1L, 1L, dateOne,
            DayOfWeek.MONDAY, Period.FIRST, auditorium, course, group,
            professor);
    private Schedule scheduleTwo = new Schedule(2L, 2L, dateOne,
            DayOfWeek.MONDAY, Period.SECOND, auditorium, course, group,
            professor);
    private Schedule scheduleThree = new Schedule(3L, 1L, dateTwo,
            DayOfWeek.MONDAY, Period.FIRST, auditorium, course, group,
            professor);

    private List<Schedule> schedules = Arrays.asList(scheduleOne, scheduleTwo,
            scheduleThree);

    private String findSql = "SELECT schedules.id, "
            + "schedules.template_id, schedules.on_date, schedules.day, "
            + "schedules.period, schedules.auditorium_id, auditoriums.name, "
            + "schedules.course_id, courses.name, schedules.group_id, groups.name, "
            + "schedules.professor_id, professors.first_name, professors.last_name "
            + "FROM schedules "
            + "LEFT JOIN auditoriums ON schedules.auditorium_id = auditoriums.id "
            + "LEFT JOIN courses ON schedules.course_id = courses.id "
            + "LEFT JOIN groups ON schedules.group_id = groups.id "
            + "LEFT JOIN professors ON schedules.professor_id = professors.id";

    @BeforeEach
    private void setUp() {
        this.scheduleRepository = new JdbcScheduleDao(jdbc);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void countShouldReturnCorrectAmountOfSchedules() {

        long expected = 3L;
        long actual = scheduleRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void findAllShouldRetrieveCorrectListOfSchedules() {

        List<Schedule> actual = scheduleRepository.findAll();

        assertThat(actual).hasSameElementsAs(schedules);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void findAllByDateShouldRetrieveCorrectListOfSchedules() {

        List<Schedule> actual = scheduleRepository.findAllByDate(dateOne);

        assertThat(actual).containsOnly(scheduleOne, scheduleTwo)
                .doesNotContain(scheduleThree);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void findAllByTemplateIdShouldRetrieveCorrectListOfSchedules() {

        List<Schedule> actual = scheduleRepository.findAllByTemplateId(1L);

        assertThat(actual).containsOnly(scheduleOne, scheduleThree)
                .doesNotContain(scheduleTwo);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void findByIdShouldReturnCorrectSchedule() {

        Schedule expectedSchedule = scheduleThree;
        Optional<Schedule> actualSchedule = scheduleRepository.findById(3L);

        assertThat(actualSchedule).isNotEmpty();
        assertThat(expectedSchedule)
                .isEqualToIgnoringNullFields(actualSchedule.get());
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Schedule> actualSchedule = scheduleRepository.findById(999L);

        assertThat(actualSchedule).isEmpty();
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test_save_all.sql")
    public void saveShouldSaveSchedule() {

        scheduleRepository.save(scheduleOne);

        String filter = " WHERE schedules.id = :id";
        SqlParameterSource paramSource = new MapSqlParameterSource("id",
                scheduleOne.getId());

        Schedule actual = jdbc.queryForObject(findSql + filter, paramSource,
                (ResultSet rs, int rowNum) -> new Schedule(rs.getLong(1),
                        rs.getLong(2), rs.getDate(3).toLocalDate(),
                        DayOfWeek.valueOf(rs.getString(4)),
                        Period.valueOf(rs.getString(5)),
                        new Auditorium(rs.getLong(6), rs.getString(7)),
                        new Course(rs.getLong(8), rs.getString(9)),
                        new Group(rs.getLong(10), rs.getString(11)),
                        new Professor(rs.getLong(12), rs.getString(13),
                                rs.getString(14))));

        assertThat(actual).isEqualTo(scheduleOne);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test_save_all.sql")
    public void saveAllShouldSaveAllSchedules() {

        scheduleRepository.saveAll(schedules);

        List<Schedule> actual = jdbc.query(findSql,
                (ResultSet rs, int rowNum) -> new Schedule(rs.getLong(1),
                        rs.getLong(2), rs.getDate(3).toLocalDate(),
                        DayOfWeek.valueOf(rs.getString(4)),
                        Period.valueOf(rs.getString(5)),
                        new Auditorium(rs.getLong(6), rs.getString(7)),
                        new Course(rs.getLong(8), rs.getString(9)),
                        new Group(rs.getLong(10), rs.getString(11)),
                        new Professor(rs.getLong(12), rs.getString(13),
                                rs.getString(14))));

        assertThat(actual).hasSameElementsAs(schedules);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void updateShouldUpdateSchedule() {

        Professor professorNew = new Professor(2L, "new", "new");
        Schedule expected = new Schedule(1L, 1L, dateTwo, DayOfWeek.WEDNESDAY,
                Period.FIFTH, auditoriumNew, course, group, professorNew);

        scheduleRepository.update(expected);

        String filter = " WHERE schedules.id = :id";

        Schedule actual = jdbc.queryForObject(findSql + filter,
                new MapSqlParameterSource("id", expected.getId()),
                (ResultSet rs, int rowNum) -> new Schedule(rs.getLong(1),
                        rs.getLong(2), rs.getDate(3).toLocalDate(),
                        DayOfWeek.valueOf(rs.getString(4)),
                        Period.valueOf(rs.getString(5)),
                        new Auditorium(rs.getLong(6), rs.getString(7)),
                        new Course(rs.getLong(8), rs.getString(9)),
                        new Group(rs.getLong(10), rs.getString(11)),
                        new Professor(rs.getLong(12), rs.getString(13),
                                rs.getString(14))));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_schedule_test.sql")
    public void updateAllWithTemplateIdShouldUpdateCorrectSchedules() {

        int deltaDays = 3;
        Schedule expectedOne = new Schedule(1L, 1L, dateOne.plusDays(3L),
                DayOfWeek.THURSDAY, Period.FIFTH, auditoriumNew, course, group,
                professor);
        Schedule expectedTwo = new Schedule(3L, 1L, dateTwo.plusDays(3L),
                DayOfWeek.THURSDAY, Period.FIFTH, auditoriumNew, course, group,
                professor);
        List<Schedule> expected = Arrays.asList(expectedOne, expectedTwo);

        scheduleRepository.updateAllWithTemplateId(expectedOne, deltaDays);

        String filter = " WHERE schedules.template_id = :templateId";
        List<Schedule> actual = jdbc.query(findSql + filter,
                new MapSqlParameterSource("templateId", 1L),
                (ResultSet rs, int rowNum) -> new Schedule(rs.getLong(1),
                        rs.getLong(2), rs.getDate(3).toLocalDate(),
                        DayOfWeek.valueOf(rs.getString(4)),
                        Period.valueOf(rs.getString(5)),
                        new Auditorium(rs.getLong(6), rs.getString(7)),
                        new Course(rs.getLong(8), rs.getString(9)),
                        new Group(rs.getLong(10), rs.getString(11)),
                        new Professor(rs.getLong(12), rs.getString(13),
                                rs.getString(14))));

        assertThat(actual).hasSameElementsAs(expected);
    }

}
