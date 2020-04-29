package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.time.DayOfWeek;
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

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.dao.jdbc.JdbcScheduleTemplateDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.ScheduleTemplate;

@JdbcTest
@ComponentScan
@Sql(scripts = "classpath:schema.sql")
class JdbcScheduleTemplateDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private ScheduleTemplateDao templateRepository;

    private Auditorium auditorium = new Auditorium(1L, "");
    private Auditorium auditoriumNew = new Auditorium(2L, "new");
    private Course course = new Course(1L, "");
    private Group group = new Group(1L, "");
    private Professor professor = new Professor(1L, "", "");

    private ScheduleTemplate templateOne = new ScheduleTemplate(1L, false,
            DayOfWeek.MONDAY, Period.FIRST, auditorium, course, group,
            professor);
    private ScheduleTemplate templateTwo = new ScheduleTemplate(2L, true,
            DayOfWeek.MONDAY, Period.SECOND, auditorium, course, group,
            professor);

    private List<ScheduleTemplate> templates = Arrays.asList(templateOne,
            templateTwo);

    @BeforeEach
    private void setUp() {
        this.templateRepository = new JdbcScheduleTemplateDao(jdbc);
    }

    @Test
    public void countShouldReturnCorrectAmountOfTemplates() {

        manuallySaveAll();
        long expected = 2L;

        long actual = templateRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldRetrieveCorrectListOfTemplates() {

        manuallySaveAll();

        List<ScheduleTemplate> actual = templateRepository.findAll();

        assertThat(actual).hasSameElementsAs(templates);
    }

    @Test
    public void findAllByDateShouldRetrieveCorrectListOfTemplates() {

        manuallySaveAll();
        boolean weekParity = false;

        List<ScheduleTemplate> actual = templateRepository
                .findAllByDate(weekParity, DayOfWeek.MONDAY);

        assertThat(actual).containsOnly(templateOne)
                .doesNotContain(templateTwo);
    }

    @Test
    public void findByIdShouldReturnCorrectTemplate() {

        manuallySaveAll();
        ScheduleTemplate expectedTemplate = templateTwo;

        Optional<ScheduleTemplate> actualTemplate = templateRepository
                .findById(templateTwo.getId());

        assertThat(actualTemplate).isNotEmpty().contains(expectedTemplate);
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        manuallySaveAll();

        Optional<ScheduleTemplate> actualTemplate = templateRepository
                .findById(999L);

        assertThat(actualTemplate).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllTemplates() {

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
        jdbc.update(sqlProfessor,
                new MapSqlParameterSource()
                        .addValue("firstName", professor.getFirstName())
                        .addValue("lastName", professor.getLastName()));

        templateRepository.saveAll(templates);

        String sql = "SELECT schedule_templates.id, "
                + "schedule_templates.week_parity, schedule_templates.day, "
                + "schedule_templates.period, schedule_templates.auditorium_id, "
                + "auditoriums.name, schedule_templates.course_id, "
                + "courses.name, schedule_templates.group_id, groups.name, "
                + "schedule_templates.professor_id, professors.first_name, "
                + "professors.last_name FROM schedule_templates "
                + "LEFT JOIN auditoriums ON schedule_templates.auditorium_id = auditoriums.id "
                + "LEFT JOIN courses ON schedule_templates.course_id = courses.id "
                + "LEFT JOIN groups ON schedule_templates.group_id = groups.id "
                + "LEFT JOIN professors ON schedule_templates.professor_id = professors.id";
        List<ScheduleTemplate> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new ScheduleTemplate(
                        rs.getLong(1), rs.getBoolean(2),
                        DayOfWeek.valueOf(rs.getString(3)),
                        Period.valueOf(rs.getString(4)),
                        new Auditorium(rs.getLong(5), rs.getString(6)),
                        new Course(rs.getLong(7), rs.getString(8)),
                        new Group(rs.getLong(9), rs.getString(10)),
                        new Professor(rs.getLong(11), rs.getString(12),
                                rs.getString(13))));

        assertThat(actual).hasSameElementsAs(templates);
    }

    @Test
    public void rescheduleShouldUpdateTemplate() {

        manuallySaveAll();

        ReschedulingOption option = new ReschedulingOption(1L, DayOfWeek.FRIDAY,
                Period.FIFTH, auditoriumNew);
        ScheduleTemplate expected = new ScheduleTemplate(1L, false,
                DayOfWeek.FRIDAY, Period.FIFTH, auditoriumNew, course, group,
                professor);

        templateRepository.reschedule(templateOne.getWeekParity(),
                templateOne.getId(), option);

        String findSql = "SELECT schedule_templates.id, "
                + "schedule_templates.week_parity, schedule_templates.day, "
                + "schedule_templates.period, schedule_templates.auditorium_id, "
                + "auditoriums.name, schedule_templates.course_id, "
                + "courses.name, schedule_templates.group_id, groups.name, "
                + "schedule_templates.professor_id, professors.first_name, "
                + "professors.last_name FROM schedule_templates "
                + "LEFT JOIN auditoriums ON schedule_templates.auditorium_id = auditoriums.id "
                + "LEFT JOIN courses ON schedule_templates.course_id = courses.id "
                + "LEFT JOIN groups ON schedule_templates.group_id = groups.id "
                + "LEFT JOIN professors ON schedule_templates.professor_id = professors.id "
                + "WHERE schedule_templates.id = :id";

        ScheduleTemplate actual = jdbc.queryForObject(findSql,
                new MapSqlParameterSource("id", expected.getId()),
                (ResultSet rs, int rowNum) -> new ScheduleTemplate(
                        rs.getLong(1), rs.getBoolean(2),
                        DayOfWeek.valueOf(rs.getString(3)),
                        Period.valueOf(rs.getString(4)),
                        new Auditorium(rs.getLong(5), rs.getString(6)),
                        new Course(rs.getLong(7), rs.getString(8)),
                        new Group(rs.getLong(9), rs.getString(10)),
                        new Professor(rs.getLong(11), rs.getString(12),
                                rs.getString(13))));

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
        jdbc.update(sqlProfessor,
                new MapSqlParameterSource()
                        .addValue("firstName", professor.getFirstName())
                        .addValue("lastName", professor.getLastName()));

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
    }

}
