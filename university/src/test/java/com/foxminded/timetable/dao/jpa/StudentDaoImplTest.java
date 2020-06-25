package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan
class StudentDaoImplTest {

    private final Group groupOne = new Group(1L, "one");
    private final Group groupTwo = new Group(2L, "two");
    private final Group groupThree = new Group(3L, "three");
    private final Student studentOne = new Student(1L, "one", "one", groupOne);
    private final Student studentTwo = new Student(2L, "two", "two", groupTwo);
    private final Student studentThree =
            new Student(3L, "three", "three", groupThree);

    @Autowired
    private StudentDao repository;

    @Test
    @Sql("classpath:sql/student_test.sql")
    public void findAllInGroupsShouldReturnCorrectListOfStudents() {

        List<Group> groups = Arrays.asList(groupOne, groupTwo);
        List<Student> actual = repository.findAllInGroups(groups);

        assertThat(actual).containsOnly(studentOne, studentTwo)
                .doesNotContain(studentThree);
    }
}