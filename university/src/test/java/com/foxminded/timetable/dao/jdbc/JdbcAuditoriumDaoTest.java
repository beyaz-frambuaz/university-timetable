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

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ScheduleTemplate;

@JdbcTest
@ComponentScan
@Sql(scripts = "classpath:schema.sql")
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
    public void countShouldReturnCorrectAmountOfAuditoriums() {

        manuallySaveAll();
        long expected = 3L;

        long actual = auditoriumRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldRetrieveCorrectListOfAuditoriums() {

        manuallySaveAll();

        List<Auditorium> actual = auditoriumRepository.findAll();

        assertThat(actual).hasSameElementsAs(auditoriums);
    }

    @Test
    public void findAllAvailableShouldRetrieveCorrectListOfAvailableAuditoriums() {

        manuallySaveAll();

        Course course = new Course(1L, "");
        Group group = new Group(1L, "");
        Professor professor = new Professor(1L, "", "");

        String insertCourseSql = "INSERT INTO courses (name) VALUES (:name)";
        jdbc.update(insertCourseSql,
                new MapSqlParameterSource("name", course.getName()));

        String insertGroupSql = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.update(insertGroupSql,
                new MapSqlParameterSource("name", group.getName()));

        String insertProfessorSql = "INSERT INTO professors "
                + "(first_name, last_name) VALUES (:firstName, :lastName)";
        jdbc.update(insertProfessorSql,
                new MapSqlParameterSource()
                        .addValue("firstName", professor.getFirstName())
                        .addValue("lastName", professor.getLastName()));

        boolean weekParity = false;
        Auditorium freeAuditoriumOne = auditoriums.get(0);
        ScheduleTemplate templateAuditoriumOne = new ScheduleTemplate(1L,
                weekParity, DayOfWeek.MONDAY, Period.FIRST, freeAuditoriumOne,
                course, group, professor);
        Auditorium freeAuditoriumTwo = auditoriums.get(1);
        ScheduleTemplate templateAuditoriumTwo = new ScheduleTemplate(2L,
                weekParity, DayOfWeek.MONDAY, Period.FIRST, freeAuditoriumTwo,
                course, group, professor);
        Auditorium busyAuditorium = auditoriums.get(2);
        ScheduleTemplate templateAuditoriumThree = new ScheduleTemplate(3L,
                weekParity, DayOfWeek.MONDAY, Period.SECOND, busyAuditorium,
                course, group, professor);
        List<ScheduleTemplate> templates = Arrays.asList(templateAuditoriumOne,
                templateAuditoriumTwo, templateAuditoriumThree);

        String insertTemplatesSql = "INSERT INTO schedule_templates "
                + "(week_parity, day, period, auditorium_id, course_id, "
                + "group_id, professor_id) VALUES (:weekParity, :day, :period, "
                + ":auditoriumId, :courseId, :groupId, :professorId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (ScheduleTemplate template : templates) {
            paramSource.add(new MapSqlParameterSource()
                    .addValue("weekParity", template.getWeekParity())
                    .addValue("day", template.getDay().toString())
                    .addValue("period", template.getPeriod().name())
                    .addValue("auditoriumId", template.getAuditorium().getId())
                    .addValue("courseId", template.getCourse().getId())
                    .addValue("groupId", template.getGroup().getId())
                    .addValue("professorId", template.getProfessor().getId()));
        }
        jdbc.batchUpdate(insertTemplatesSql, paramSource
                .toArray(new SqlParameterSource[paramSource.size()]));

        List<Auditorium> actual = auditoriumRepository.findAllAvailable(
                weekParity, LocalDate.of(2020, 9, 7), Period.SECOND);

        assertThat(actual).containsOnly(freeAuditoriumOne, freeAuditoriumTwo)
                .doesNotContain(busyAuditorium);
    }

    @Test
    public void findByIdShouldReturnCorrectAuditorium() {

        manuallySaveAll();
        Auditorium expectedAuditorium = new Auditorium(3L, "three");

        Optional<Auditorium> actualAuditorium = auditoriumRepository
                .findById(3L);

        assertThat(actualAuditorium).isNotEmpty().contains(expectedAuditorium);
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        manuallySaveAll();

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
    
    private void manuallySaveAll() {
        
        String sql = "INSERT INTO auditoriums (name) VALUES (:name)";
        jdbc.batchUpdate(sql, SqlParameterSourceUtils.createBatch(auditoriums));
    }

}
