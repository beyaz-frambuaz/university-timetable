package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ReschedulingOption;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;

import java.time.DayOfWeek;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan
class ReschedulingOptionDaoImplTest {

    private final Auditorium auditorium = new Auditorium(1L, "one");
    private final ReschedulingOption optionOne =
            new ReschedulingOption(1L, DayOfWeek.MONDAY, Period.FIRST,
                    auditorium);
    private final ReschedulingOption optionTwo =
            new ReschedulingOption(2L, DayOfWeek.MONDAY, Period.SECOND,
                    auditorium);
    private final ReschedulingOption optionThree =
            new ReschedulingOption(3L, DayOfWeek.TUESDAY, Period.FIRST,
                    auditorium);

    @Autowired
    private ReschedulingOptionDao repository;


    @Test
    @Sql("classpath:sql/rescheduling_option_test.sql")
    public void findAllByDayShouldReturnCorrectListOfOptions() {

        List<ReschedulingOption> actual =
                repository.findAllByDay(DayOfWeek.MONDAY);

        assertThat(actual).containsOnly(optionOne, optionTwo)
                .doesNotContain(optionThree);
    }

}