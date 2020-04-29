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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.test.context.jdbc.Sql;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.model.ScheduleTemplate;

@JdbcTest
@ComponentScan
@Sql(scripts = "classpath:schema.sql")
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
    private Group groupTwo = new Group(1L, "two");
    private List<Group> groups = Arrays.asList(groupOne, groupTwo);
    private Professor professorOne = new Professor(1L, "one", "one");
    private Professor professorTwo = new Professor(2L, "two", "two");
    private List<Professor> professors = Arrays.asList(professorOne,
            professorTwo);
    private ScheduleTemplate templateOne = new ScheduleTemplate(1L, false,
            DayOfWeek.MONDAY, Period.FIRST, auditoriumOne, course, groupOne,
            professorOne);
    private ScheduleTemplate templateTwo = new ScheduleTemplate(2L, false,
            DayOfWeek.MONDAY, Period.SECOND, auditoriumOne, course, groupTwo,
            professorOne);
    private ScheduleTemplate templateThree = new ScheduleTemplate(3L, false,
            DayOfWeek.MONDAY, Period.THIRD, auditoriumOne, course, groupOne,
            professorTwo);
    private List<ScheduleTemplate> templates = Arrays.asList(templateOne,
            templateTwo, templateThree);
    private Schedule schedule = new Schedule(1L, templateOne, date);

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
    public void countShouldReturnCorrectAmountOfOptions() {

        manuallySaveAll();
        long expected = 8L;

        long actual = optionRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldRetrieveCorrectListOfOptions() {

        manuallySaveAll();

        List<ReschedulingOption> actual = optionRepository.findAll();

        assertThat(actual).hasSameElementsAs(options);
    }

    @Test
    public void findDayReschedulingOptionsForScheduleShouldFindNoConflictOptions() {

        manuallySaveAll();

        manuallySaveDependencies();

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
    public void findByIdShouldReturnCorrectOption() {

        manuallySaveAll();
        ReschedulingOption expectedOption = new ReschedulingOption(1L,
                DayOfWeek.MONDAY, Period.FIRST, auditoriumOne);

        Optional<ReschedulingOption> actualOption = optionRepository
                .findById(expectedOption.getId());

        assertThat(actualOption).isNotEmpty().contains(expectedOption);
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        manuallySaveAll();

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

    private void manuallySaveAll() {

        String sqlAuditorium = "INSERT INTO auditoriums (name) VALUES (:name)";
        jdbc.batchUpdate(sqlAuditorium,
                SqlParameterSourceUtils.createBatch(auditoriums));

        String sqlOption = "INSERT INTO rescheduling_options (day, period, "
                + "auditorium_id) VALUES (:day, :period, :auditoriumId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (ReschedulingOption option : options) {
            paramSource.add(new MapSqlParameterSource()
                    .addValue("day", option.getDay().toString())
                    .addValue("period", option.getPeriod().name())
                    .addValue("auditoriumId", option.getAuditorium().getId()));
        }
        jdbc.batchUpdate(sqlOption, paramSource
                .toArray(new SqlParameterSource[paramSource.size()]));
    }

    private void manuallySaveDependencies() {

        String sqlCourse = "INSERT INTO courses (name) VALUES (:name)";
        jdbc.update(sqlCourse,
                new MapSqlParameterSource("name", course.getName()));

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.batchUpdate(sqlGroup, SqlParameterSourceUtils.createBatch(groups));

        String sqlProfessor = "INSERT INTO professors (first_name, "
                + "last_name) VALUES (:firstName, :lastName)";
        jdbc.batchUpdate(sqlProfessor,
                SqlParameterSourceUtils.createBatch(professors));

        String sqlTemplate = "INSERT INTO schedule_templates (week_parity, day, "
                + "period, auditorium_id, course_id, group_id, professor_id) "
                + "VALUES (:weekParity, :day, :period, :auditoriumId, "
                + ":courseId, :groupId, :professorId)";
        List<SqlParameterSource> paramTemplate = new ArrayList<>();
        for (ScheduleTemplate template : templates) {
            paramTemplate.add(new MapSqlParameterSource()
                    .addValue("weekParity", template.getWeekParity())
                    .addValue("day", template.getDay().toString())
                    .addValue("period", template.getPeriod().name())
                    .addValue("auditoriumId", template.getAuditorium().getId())
                    .addValue("courseId", template.getCourse().getId())
                    .addValue("groupId", template.getGroup().getId())
                    .addValue("professorId", template.getProfessor().getId()));
        }
        jdbc.batchUpdate(sqlTemplate, paramTemplate
                .toArray(new SqlParameterSource[paramTemplate.size()]));

        String sqlSchedule = "INSERT INTO schedules (template_id, on_date, day, "
                + "period, auditorium_id, course_id, group_id, professor_id) "
                + "VALUES (:templateId, :date, :day, :period, :auditoriumId, "
                + ":courseId, :groupId, :professorId)";
        SqlParameterSource paramSchedule = new MapSqlParameterSource()
                .addValue("templateId", schedule.getScheduleTemplate().getId())
                .addValue("date", schedule.getDate().toString())
                .addValue("day", schedule.getDay().toString())
                .addValue("period", schedule.getPeriod().name())
                .addValue("auditoriumId", schedule.getAuditorium().getId())
                .addValue("courseId", schedule.getCourse().getId())
                .addValue("groupId", schedule.getGroup().getId())
                .addValue("professorId", schedule.getProfessor().getId());
        jdbc.update(sqlSchedule, paramSchedule);
    }

}
