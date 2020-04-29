package com.foxminded.timetable.dao.jdbc;

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

import com.foxminded.timetable.dao.ScheduleDao;
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
class JdbcScheduleDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private ScheduleDao scheduleRepository;

    private LocalDate dateOne = LocalDate.of(2020, 9, 7);
    private LocalDate dateTwo = LocalDate.of(2020, 9, 14);

    private Auditorium auditorium = new Auditorium(1L, "");
    private Auditorium auditoriumNew = new Auditorium(2L, "new");
    private Course course = new Course(1L, "");
    private Group group = new Group(1L, "");
    private Professor professorOriginal = new Professor(1L, "", "");
    private Professor professorNew = new Professor(2L, "new", "new");

    private ScheduleTemplate templateOne = new ScheduleTemplate(1L, false,
            DayOfWeek.MONDAY, Period.FIRST, auditorium, course, group,
            professorOriginal);
    private ScheduleTemplate templateTwo = new ScheduleTemplate(2L, true,
            DayOfWeek.MONDAY, Period.SECOND, auditorium, course, group,
            professorOriginal);

    private Schedule scheduleOne = new Schedule(1L, templateOne, dateOne);
    private Schedule scheduleTwo = new Schedule(2L, templateTwo, dateOne);
    private Schedule scheduleThree = new Schedule(3L, templateOne, dateTwo);

    private List<Schedule> schedules = Arrays.asList(scheduleOne, scheduleTwo,
            scheduleThree);

    @BeforeEach
    private void setUp() {
        this.scheduleRepository = new JdbcScheduleDao(jdbc);
    }

    @Test
    public void countShouldReturnCorrectAmountOfSchedules() {

        manuallySaveAll();
        long expected = 3L;

        long actual = scheduleRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldRetrieveCorrectListOfSchedules() {

        manuallySaveAll();

        List<Schedule> actual = scheduleRepository.findAll();

        assertThat(actual)
                .usingElementComparatorIgnoringFields("scheduleTemplate")
                .hasSameElementsAs(schedules);
    }

    @Test
    public void findAllByDateShouldRetrieveCorrectListOfSchedules() {

        manuallySaveAll();

        List<Schedule> actual = scheduleRepository.findAllByDate(dateOne);

        assertThat(actual)
                .usingElementComparatorIgnoringFields("scheduleTemplate")
                .containsOnly(scheduleOne, scheduleTwo)
                .doesNotContain(scheduleThree);
    }

    @Test
    public void findAllByTemplateIdShouldRetrieveCorrectListOfSchedules() {

        manuallySaveAll();

        List<Schedule> actual = scheduleRepository.findAllByTemplateId(1L);

        assertThat(actual)
                .usingElementComparatorIgnoringFields("scheduleTemplate")
                .containsOnly(scheduleOne, scheduleThree)
                .doesNotContain(scheduleTwo);
    }

    @Test
    public void findByIdShouldReturnCorrectSchedule() {

        manuallySaveAll();
        Schedule expectedSchedule = scheduleThree;

        Optional<Schedule> actualSchedule = scheduleRepository.findById(3L);

        assertThat(actualSchedule).isNotEmpty();
        assertThat(expectedSchedule)
                .isEqualToIgnoringNullFields(actualSchedule.get());
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        manuallySaveAll();

        Optional<Schedule> actualSchedule = scheduleRepository.findById(999L);

        assertThat(actualSchedule).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllAuditoriums() {

        String sqlAuditorium = "INSERT INTO auditoriums (name) VALUES (:name)";
        jdbc.update(sqlAuditorium,
                new MapSqlParameterSource("name", auditorium.getName()));

        String sqlCourse = "INSERT INTO courses (name) VALUES (:name)";
        jdbc.update(sqlCourse,
                new MapSqlParameterSource("name", course.getName()));

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.update(sqlGroup,
                new MapSqlParameterSource("name", group.getName()));

        String sqlProfessor = "INSERT INTO professors (first_name, "
                + "last_name) VALUES (:firstName, :lastName)";
        jdbc.batchUpdate(sqlProfessor, SqlParameterSourceUtils
                .createBatch(Arrays.asList(professorOriginal, professorNew)));

        String sqlTemplate = "INSERT INTO schedule_templates (week_parity, day, "
                + "period, auditorium_id, course_id, group_id, professor_id) "
                + "VALUES (:weekParity, :day, :period, :auditoriumId, "
                + ":courseId, :groupId, :professorId)";
        List<SqlParameterSource> paramTemplate = new ArrayList<>();
        for (ScheduleTemplate template : Arrays.asList(templateOne,
                templateTwo)) {
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

        scheduleRepository.saveAll(schedules);

        String sql = "SELECT schedules.id, schedules.template_id, "
                + "schedules.on_date, schedules.day, schedules.period, "
                + "schedules.auditorium_id, auditoriums.name, "
                + "schedules.course_id, courses.name, schedules.group_id, "
                + "groups.name, schedules.professor_id, professors.first_name, "
                + "professors.last_name FROM schedules "
                + "LEFT JOIN auditoriums ON schedules.auditorium_id = auditoriums.id "
                + "LEFT JOIN courses ON schedules.course_id = courses.id "
                + "LEFT JOIN groups ON schedules.group_id = groups.id "
                + "LEFT JOIN professors ON schedules.professor_id = professors.id";
        List<Schedule> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new Schedule(rs.getLong(1),
                        rs.getLong(2), rs.getDate(3).toLocalDate(),
                        DayOfWeek.valueOf(rs.getString(4)),
                        Period.valueOf(rs.getString(5)),
                        new Auditorium(rs.getLong(6), rs.getString(7)),
                        new Course(rs.getLong(8), rs.getString(9)),
                        new Group(rs.getLong(10), rs.getString(11)),
                        new Professor(rs.getLong(12), rs.getString(13),
                                rs.getString(14))));

        assertThat(actual)
                .usingElementComparatorIgnoringFields("scheduleTemplate")
                .hasSameElementsAs(schedules);
    }

    @Test
    public void rescheduleShouldUpdateSchedule() {

        manuallySaveAll();

        ReschedulingOption option = new ReschedulingOption(1L, DayOfWeek.MONDAY,
                Period.FIFTH, auditoriumNew);
        Schedule expected = new Schedule(1L, 1L, dateTwo, DayOfWeek.MONDAY,
                Period.FIFTH, auditoriumNew, course, group, professorOriginal);

        scheduleRepository.reschedule(scheduleOne, dateTwo, option);

        String findSql = "SELECT schedules.id, "
                + "schedules.template_id, schedules.on_date, schedules.day, "
                + "schedules.period, schedules.auditorium_id, auditoriums.name, "
                + "schedules.course_id, courses.name, schedules.group_id, groups.name, "
                + "schedules.professor_id, professors.first_name, professors.last_name "
                + "FROM schedules "
                + "LEFT JOIN auditoriums ON schedules.auditorium_id = auditoriums.id "
                + "LEFT JOIN courses ON schedules.course_id = courses.id "
                + "LEFT JOIN groups ON schedules.group_id = groups.id "
                + "LEFT JOIN professors ON schedules.professor_id = professors.id "
                + " WHERE schedules.id = :id";

        Schedule actual = jdbc.queryForObject(findSql,
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
    public void updateAllWithTemplateIdShouldUpdateCorrectSchedules() {

        manuallySaveAll();

        ReschedulingOption option = new ReschedulingOption(1L,
                DayOfWeek.THURSDAY, Period.FIFTH, auditoriumNew);
        int deltaDays = 3;
        Schedule expectedOne = new Schedule(1L, templateOne.getId(),
                dateOne.plusDays(3L), DayOfWeek.THURSDAY, Period.FIFTH,
                auditoriumNew, course, group, professorOriginal);
        Schedule expectedTwo = new Schedule(3L, templateOne.getId(),
                dateTwo.plusDays(3L), DayOfWeek.THURSDAY, Period.FIFTH,
                auditoriumNew, course, group, professorOriginal);
        List<Schedule> expected = Arrays.asList(expectedOne, expectedTwo);

        scheduleRepository.updateAllWithTemplateId(templateOne.getId(), option,
                deltaDays);

        String findSql = "SELECT schedules.id, "
                + "schedules.template_id, schedules.on_date, schedules.day, "
                + "schedules.period, schedules.auditorium_id, auditoriums.name, "
                + "schedules.course_id, courses.name, schedules.group_id, groups.name, "
                + "schedules.professor_id, professors.first_name, professors.last_name "
                + "FROM schedules "
                + "LEFT JOIN auditoriums ON schedules.auditorium_id = auditoriums.id "
                + "LEFT JOIN courses ON schedules.course_id = courses.id "
                + "LEFT JOIN groups ON schedules.group_id = groups.id "
                + "LEFT JOIN professors ON schedules.professor_id = professors.id "
                + " WHERE schedules.template_id = :templateId";
        List<Schedule> actual = jdbc.query(findSql,
                new MapSqlParameterSource("templateId", templateOne.getId()),
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

    @Test
    public void substituteProfessorShouldUpdateProfessorReference() {

        manuallySaveAll();

        Schedule expected = new Schedule(1L, templateOne.getId(), dateOne,
                DayOfWeek.MONDAY, Period.FIRST, auditorium, course, group,
                professorNew);

        scheduleRepository.substituteProfessor(scheduleOne.getId(),
                professorNew.getId());
        
        String findSql = "SELECT schedules.id, "
                + "schedules.template_id, schedules.on_date, schedules.day, "
                + "schedules.period, schedules.auditorium_id, auditoriums.name, "
                + "schedules.course_id, courses.name, schedules.group_id, groups.name, "
                + "schedules.professor_id, professors.first_name, professors.last_name "
                + "FROM schedules "
                + "LEFT JOIN auditoriums ON schedules.auditorium_id = auditoriums.id "
                + "LEFT JOIN courses ON schedules.course_id = courses.id "
                + "LEFT JOIN groups ON schedules.group_id = groups.id "
                + "LEFT JOIN professors ON schedules.professor_id = professors.id "
                + " WHERE schedules.id = :id";

        Schedule actual = jdbc.queryForObject(findSql,
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

    private void manuallySaveAll() {

        String sqlAuditorium = "INSERT INTO auditoriums (name) VALUES (:name)";
        jdbc.batchUpdate(sqlAuditorium, SqlParameterSourceUtils
                .createBatch(Arrays.asList(auditorium, auditoriumNew)));

        String sqlCourse = "INSERT INTO courses (name) VALUES (:name)";
        jdbc.update(sqlCourse,
                new MapSqlParameterSource("name", course.getName()));

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.update(sqlGroup,
                new MapSqlParameterSource("name", group.getName()));

        String sqlProfessor = "INSERT INTO professors (first_name, "
                + "last_name) VALUES (:firstName, :lastName)";
        jdbc.batchUpdate(sqlProfessor, SqlParameterSourceUtils
                .createBatch(Arrays.asList(professorOriginal, professorNew)));

        String sqlTemplate = "INSERT INTO schedule_templates (week_parity, day, "
                + "period, auditorium_id, course_id, group_id, professor_id) "
                + "VALUES (:weekParity, :day, :period, :auditoriumId, "
                + ":courseId, :groupId, :professorId)";
        List<SqlParameterSource> paramTemplate = new ArrayList<>();
        for (ScheduleTemplate template : Arrays.asList(templateOne,
                templateTwo)) {
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
        List<SqlParameterSource> paramSchedule = new ArrayList<>();
        for (Schedule schedule : schedules) {
            paramSchedule.add(new MapSqlParameterSource()
                    .addValue("templateId",
                            schedule.getScheduleTemplate().getId())
                    .addValue("date", schedule.getDate().toString())
                    .addValue("day", schedule.getDay().toString())
                    .addValue("period", schedule.getPeriod().name())
                    .addValue("auditoriumId", schedule.getAuditorium().getId())
                    .addValue("courseId", schedule.getCourse().getId())
                    .addValue("groupId", schedule.getGroup().getId())
                    .addValue("professorId", schedule.getProfessor().getId()));
        }
        jdbc.batchUpdate(sqlSchedule, paramSchedule
                .toArray(new SqlParameterSource[paramSchedule.size()]));
    }
}
