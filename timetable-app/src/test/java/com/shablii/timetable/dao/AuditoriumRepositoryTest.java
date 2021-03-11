package com.shablii.timetable.dao;

import com.shablii.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AuditoriumRepositoryTest {

    private final Auditorium auditoriumOne = new Auditorium(1L, "one");
    private final Auditorium auditoriumTwo = new Auditorium(2L, "two");
    private final Auditorium auditoriumThree = new Auditorium(3L, "three");

    @Autowired
    private AuditoriumRepository repository;

    @Test
    @Sql("classpath:sql/auditorium_test.sql")
    public void findAllAvailableShouldRetrieveCorrectListOfAvailableAuditoriums() {

        List<Auditorium> actual = repository.findAllAvailable(LocalDate.of(2020, 9, 7), Period.SECOND);

        assertThat(actual).containsOnly(auditoriumOne, auditoriumTwo).doesNotContain(auditoriumThree);
    }

}