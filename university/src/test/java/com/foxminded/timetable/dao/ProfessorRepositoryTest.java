package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ProfessorRepositoryTest {

    private final Professor professorOne = new Professor(1L, "one", "one");
    private final Professor professorTwo = new Professor(2L, "two", "two");
    private final Professor professorThree =
            new Professor(3L, "three", "three");

    @Autowired
    private ProfessorRepository repository;

    @Test
    @Sql("classpath:sql/professor_test.sql")
    public void findAllAvailableShouldRetrieveCorrectListOfAvailableProfessors() {

        List<Professor> actual =
                repository.findAllAvailable(LocalDate.of(2020, 9, 7),
                        Period.SECOND);

        assertThat(actual).containsOnly(professorOne, professorTwo)
                .doesNotContain(professorThree);
    }

}