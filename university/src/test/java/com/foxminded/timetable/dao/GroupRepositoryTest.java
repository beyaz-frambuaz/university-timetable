package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Group;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class GroupRepositoryTest {

    private final Group groupOne = new Group(1L, "one");
    private final Group groupTwo = new Group(2L, "two");
    private final Group groupThree = new Group(3L, "three");

    @Autowired
    private GroupRepository repository;

    @Test
    @Sql("classpath:sql/group_test.sql")
    public void findAllByProfessorAndCourseShouldReturnCorrectListOfGroups() {

        List<Group> actual = repository.findAllByProfessorAndCourse(1L, 1L);

        assertThat(actual).containsOnly(groupOne, groupThree)
                .doesNotContain(groupTwo);
    }

}