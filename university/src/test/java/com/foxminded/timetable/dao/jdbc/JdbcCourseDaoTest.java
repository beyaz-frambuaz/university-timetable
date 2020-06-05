package com.foxminded.timetable.dao.jdbc;

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcCourseDaoTest {

    private final Course                     courseOne   = new Course(1L,
            "one");
    private final Course                     courseTwo   = new Course(2L,
            "two");
    private final Course                     courseThree = new Course(3L,
            "three");
    private final List<Course>               courses     = Arrays.asList(
            courseOne, courseTwo, courseThree);
    @Autowired
    private       NamedParameterJdbcTemplate jdbc;
    private       CourseDao                  courseRepository;

    @BeforeEach
    private void setUp() {

        this.courseRepository = new JdbcCourseDao(jdbc);
    }

    @Test
    @Sql("classpath:preload_sample_data_course_test.sql")
    public void countShouldReturnCorrectAmountOfCourses() {

        long expected = 3L;
        long actual = courseRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_course_test.sql")
    public void findAllShouldRetrieveCorrectListOfCourses() {

        List<Course> actual = courseRepository.findAll();

        assertThat(actual).hasSameElementsAs(courses);
    }

    @Test
    @Sql("classpath:preload_sample_data_course_test.sql")
    public void findByIdShouldReturnCorrectCourse() {

        Course expectedCourse = new Course(3L, "three");
        Optional<Course> actualCourse = courseRepository.findById(3L);

        assertThat(actualCourse).isNotEmpty().contains(expectedCourse);
    }

    @Test
    @Sql("classpath:preload_sample_data_course_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Course> actualCourse = courseRepository.findById(999L);

        assertThat(actualCourse).isEmpty();
    }

    @Test
    public void saveShouldAddCourse() {

        Course expected = courseRepository.save(courseOne);

        String sql = "SELECT courses.id, courses.name FROM courses "
                + "WHERE courses.id = :id";
        Course actual = jdbc.queryForObject(sql,
                new MapSqlParameterSource("id", courseOne.getId()),
                (ResultSet rs, int rowNum) -> new Course(rs.getLong(1),
                        rs.getString(2)));

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @Sql("classpath:preload_sample_data_course_test.sql")
    public void updateShouldUpdateCourseName() {

        String newName = "new name";
        Course expected = new Course(courseOne.getId(), courseOne.getName());
        expected.setName(newName);

        courseRepository.update(expected);

        String sql = "SELECT courses.id, courses.name FROM courses "
                + "WHERE courses.id = :id";
        Optional<Course> actual = Optional.of(jdbc.queryForObject(sql,
                new MapSqlParameterSource("id", courseOne.getId()),
                (ResultSet rs, int rowNum) -> new Course(rs.getLong(1),
                        rs.getString(2))));

        assertThat(actual).isPresent().contains(expected);
    }

    @Test
    public void saveAllShouldSaveAllCourses() {

        courseRepository.saveAll(courses);

        String sql = "SELECT courses.id, courses.name FROM courses";
        List<Course> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new Course(rs.getLong(1),
                        rs.getString(2)));

        assertThat(actual).hasSameElementsAs(courses);
    }

}
