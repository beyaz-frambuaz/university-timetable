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

import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ScheduleTemplate;

@JdbcTest
@ComponentScan
@Sql(scripts = "classpath:schema.sql")
class JdbcProfessorDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private ProfessorDao professorRepository;

    private List<Professor> professors = Arrays.asList(
            new Professor(1L, "one", "one"), new Professor(2L, "two", "two"),
            new Professor(3L, "three", "three"));

    @BeforeEach
    private void setUp() {
        this.professorRepository = new JdbcProfessorDao(jdbc);
    }

    @Test
    public void countShouldReturnCorrectAmountOfProfessors() {

        manuallySaveAll();
        long expected = 3L;

        long actual = professorRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldRetrieveCorrectListOfProfessors() {

        manuallySaveAll();

        List<Professor> actual = professorRepository.findAll();

        assertThat(actual).hasSameElementsAs(professors);
    }

    @Test
    public void findAllAvailableShouldRetrieveCorrectListOfAvailableProfessors() {

        manuallySaveAll();

        Course course = new Course(1L, "");
        Group group = new Group(1L, "");
        Auditorium auditorium = new Auditorium(1L, "");

        String insertCourseSql = "INSERT INTO courses (name) VALUES (:name)";
        jdbc.update(insertCourseSql,
                new MapSqlParameterSource("name", course.getName()));

        String insertGroupSql = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.update(insertGroupSql,
                new MapSqlParameterSource("name", group.getName()));

        String insertAuditoriumSql = "INSERT INTO auditoriums (name) "
                + "VALUES (:name)";
        jdbc.update(insertAuditoriumSql,
                new MapSqlParameterSource("name", auditorium.getName()));

        boolean weekParity = false;
        Professor freeProfessorOne = professors.get(0);
        ScheduleTemplate templateProfessorOne = new ScheduleTemplate(1L,
                weekParity, DayOfWeek.MONDAY, Period.FIRST, auditorium, course,
                group, freeProfessorOne);
        Professor freeProfessorTwo = professors.get(1);
        ScheduleTemplate templateProfessorTwo = new ScheduleTemplate(2L,
                weekParity, DayOfWeek.MONDAY, Period.FIRST, auditorium, course,
                group, freeProfessorTwo);
        Professor busyProfessor = professors.get(2);
        ScheduleTemplate templateProfessorThree = new ScheduleTemplate(3L,
                weekParity, DayOfWeek.MONDAY, Period.SECOND, auditorium, course,
                group, busyProfessor);
        List<ScheduleTemplate> templates = Arrays.asList(templateProfessorOne,
                templateProfessorTwo, templateProfessorThree);

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

        List<Professor> actual = professorRepository.findAllAvailable(
                weekParity, LocalDate.of(2020, 9, 7), Period.SECOND);

        assertThat(actual).containsOnly(freeProfessorOne, freeProfessorTwo)
                .doesNotContain(busyProfessor);
    }

    @Test
    public void findByIdShouldReturnCorrectProfessor() {

        manuallySaveAll();
        Professor expectedProfessor = new Professor(3L, "three", "three");

        Optional<Professor> actualProfessor = professorRepository.findById(3L);

        assertThat(actualProfessor).isNotEmpty().contains(expectedProfessor);
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        manuallySaveAll();

        Optional<Professor> actualProfessor = professorRepository
                .findById(999L);

        assertThat(actualProfessor).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllProfessors() {

        professorRepository.saveAll(professors);

        String sql = "SELECT professors.id, professors.first_name, "
                + "professors.last_name FROM professors";
        List<Professor> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new Professor(rs.getLong(1),
                        rs.getString(2), rs.getString(3)));

        assertThat(actual).hasSameElementsAs(professors);
    }
    
    @Test
    public void saveAllProfessorsCoursesShouldSaveAllCourseAssignments() {
        
        Course courseOne = new Course(1L, "one");
        Course courseTwo = new Course(2L, "two");
        String sqlCourses = "INSERT INTO courses (name) VALUES (:name)";
        jdbc.batchUpdate(sqlCourses,
                SqlParameterSourceUtils.createBatch(courseOne, courseTwo));

        List<Professor> professorsWithCourses = new ArrayList<>(professors);
        professorsWithCourses.get(0).setCourses(Arrays.asList(courseOne));
        professorsWithCourses.get(1)
                .setCourses(Arrays.asList(courseOne, courseTwo));
        professorsWithCourses.get(2).setCourses(Arrays.asList(courseTwo));
        
        manuallySaveAll();
        
        professorRepository.saveAllProfessorsCourses(professorsWithCourses);
        
        String findAllSql = "SELECT professors.id, "
                + "professors.first_name, professors.last_name, courses.id, "
                + "courses.name FROM professors LEFT JOIN professors_courses "
                + "ON professors.id = professors_courses.professor_id "
                + "LEFT JOIN courses "
                + "ON courses.id = professors_courses.course_id";
        List<Professor> actual = jdbc.query(findAllSql, (ResultSet rs) -> {
            
            List<Professor> professors = new ArrayList<>();
            Professor current = null;
            while (rs.next()) {
                long currentId = rs.getLong(1);
                if (current == null) {
                    current = new Professor(currentId, rs.getString(2),
                            rs.getString(3));
                } else if (current.getId() != currentId) {
                    professors.add(current);
                    current = new Professor(currentId, rs.getString(2),
                            rs.getString(3));
                }
                Long courseId = rs.getLong(4);
                String courseName = rs.getString(5);
                if (courseId != null && courseName != null) {
                    current.addCourse(new Course(courseId, courseName));
                }
            }
            if (current != null) {
                professors.add(current);
            }

            return professors;
        });
        
        assertThat(actual).hasSameElementsAs(professorsWithCourses);
    }
    
    private void manuallySaveAll() {
        
        String sql = "INSERT INTO professors (first_name, last_name) "
                + "VALUES (:firstName, :lastName)";
        jdbc.batchUpdate(sql, SqlParameterSourceUtils.createBatch(professors));
    }

}
