package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
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
    @Sql("classpath:preload_sample_data_professor_test.sql")
    public void countShouldReturnCorrectAmountOfProfessors() {

        long expected = 3L;
        long actual = professorRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_professor_test.sql")
    public void findAllShouldRetrieveCorrectListOfProfessors() {

        List<Professor> actual = professorRepository.findAll();

        assertThat(actual).hasSameElementsAs(professors);
    }

    @Test
    @Sql("classpath:preload_sample_data_professor_test.sql")
    public void findAllAvailableShouldRetrieveCorrectListOfAvailableProfessors() {

        Professor freeProfessorOne = professors.get(0);
        Professor freeProfessorTwo = professors.get(1);
        Professor busyProfessor = professors.get(2);

        List<Professor> actual = professorRepository.findAllAvailable(
                false, LocalDate.of(2020, 9, 7), Period.SECOND);

        assertThat(actual).containsOnly(freeProfessorOne, freeProfessorTwo)
                .doesNotContain(busyProfessor);
    }

    @Test
    @Sql("classpath:preload_sample_data_professor_test.sql")
    public void findByIdShouldReturnCorrectProfessor() {

        Professor expectedProfessor = new Professor(3L, "three", "three");
        Optional<Professor> actualProfessor = professorRepository.findById(3L);

        assertThat(actualProfessor).isNotEmpty().contains(expectedProfessor);
    }

    @Test
    @Sql("classpath:preload_sample_data_professor_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

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
    @Sql("classpath:preload_sample_data_professor_test.sql")
    public void saveAllProfessorsCoursesShouldSaveAllCourseAssignments() {
        
        Course courseOne = new Course(1L, "one");
        Course courseTwo = new Course(2L, "two");

        List<Professor> professorsWithCourses = new ArrayList<>(professors);
        professorsWithCourses.get(0).setCourses(Arrays.asList(courseOne));
        professorsWithCourses.get(1)
                .setCourses(Arrays.asList(courseOne, courseTwo));
        professorsWithCourses.get(2).setCourses(Arrays.asList(courseTwo));
        
        
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

}
