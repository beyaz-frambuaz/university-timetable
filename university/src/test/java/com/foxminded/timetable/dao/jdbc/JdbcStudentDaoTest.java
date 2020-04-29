package com.foxminded.timetable.dao.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSet;
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

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;

@JdbcTest
@ComponentScan
@Sql(scripts = "classpath:schema.sql")
class JdbcStudentDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate jdbc;

    private StudentDao studentRepository;

    private Group groupOne = new Group(1L, "");
    private Group groupTwo = new Group(2L, "");
    private List<Group> groups = Arrays.asList(groupOne, groupTwo);

    private Student studentOne = new Student(1L, "one", "one", groupOne);
    private Student studentTwo = new Student(2L, "two", "two", groupOne);
    private Student studentThree = new Student(3L, "three", "three", groupTwo);
    private List<Student> students = Arrays.asList(studentOne, studentTwo,
            studentThree);

    @BeforeEach
    private void setUp() {
        this.studentRepository = new JdbcStudentDao(jdbc);
    }

    @Test
    public void countShouldReturnCorrectAmountOfStudents() {

        manuallySaveAll();
        long expected = 3L;

        long actual = studentRepository.count();

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldRetrieveCorrectListOfStudents() {

        manuallySaveAll();

        List<Student> actual = studentRepository.findAll();

        assertThat(actual).hasSameElementsAs(students);
    }

    @Test
    public void findAllByGroupsShouldRetrieveCorrectListOfStudents() {

        manuallySaveAll();
        List<Group> requestedGroups = Arrays.asList(groupOne);

        List<Student> actual = studentRepository
                .findAllByGroups(requestedGroups);

        assertThat(actual).containsOnly(studentOne, studentTwo)
                .doesNotContain(studentThree);
    }

    @Test
    public void findByIdShouldReturnCorrectStudent() {

        manuallySaveAll();
        Student expectedStudent = studentThree;

        Optional<Student> actualStudent = studentRepository
                .findById(expectedStudent.getId());

        assertThat(actualStudent).isNotEmpty().contains(expectedStudent);
    }

    @Test
    public void findByIdShouldReturnEmptyOptionalGivenNonExistingId() {

        manuallySaveAll();

        Optional<Student> actualStudent = studentRepository.findById(999L);

        assertThat(actualStudent).isEmpty();
    }

    @Test
    public void saveAllShouldSaveAllStudents() {

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.batchUpdate(sqlGroup, SqlParameterSourceUtils.createBatch(groups));

        studentRepository.saveAll(students);

        String sql = "SELECT students.id, students.first_name, "
                + "students.last_name, groups.id, groups.name FROM students "
                + "LEFT JOIN groups ON students.group_id = groups.id";
        List<Student> actual = jdbc.query(sql,
                (ResultSet rs, int rowNum) -> new Student(rs.getLong(1),
                        rs.getString(2), rs.getString(3),
                        new Group(rs.getLong(4), rs.getString(5))));

        assertThat(actual).hasSameElementsAs(students);
    }

    private void manuallySaveAll() {

        String sqlGroup = "INSERT INTO groups (name) VALUES (:name)";
        jdbc.batchUpdate(sqlGroup, SqlParameterSourceUtils.createBatch(groups));

        String sql = "INSERT INTO students (first_name, last_name, group_id) "
                + "VALUES (:firstName, :lastName, :groupId)";
        List<SqlParameterSource> paramSource = new ArrayList<>();
        for (Student student : students) {
            paramSource.add(new MapSqlParameterSource()
                    .addValue("firstName", student.getFirstName())
                    .addValue("lastName", student.getLastName())
                    .addValue("groupId", student.getGroup().getId()));
        }
        jdbc.batchUpdate(sql, paramSource
                .toArray(new SqlParameterSource[paramSource.size()]));
    }

}
