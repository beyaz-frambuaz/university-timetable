package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
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

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;

@JdbcTest
@ComponentScan
@Sql("classpath:schema.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
class JdbcCourseDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private CourseDao courseRepository;

    private List<Course> courses = Arrays.asList(new Course(1L, "one"),
            new Course(2L, "two"), new Course(3L, "three"));
    
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
        Optional<Course> actualCourse = courseRepository
                .findById(3L);

        assertThat(actualCourse).isNotEmpty().contains(expectedCourse);
    }

    @Test
    @Sql("classpath:preload_sample_data_course_test.sql")
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        Optional<Course> actualCourse = courseRepository
                .findById(999L);

        assertThat(actualCourse).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllCourses() {

        courseRepository.saveAll(courses);

        String sql = "SELECT courses.id, courses.name FROM courses";
        List<Course> actual = jdbc.query(sql, (ResultSet rs,
                int rowNum) -> new Course(rs.getLong(1), rs.getString(2)));

        assertThat(actual).hasSameElementsAs(courses);
    }

}
