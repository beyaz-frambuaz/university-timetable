package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.jdbc.Sql;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ComponentScan
class ScheduleDaoImplTest {

    private final LocalDate dateOne = LocalDate.of(2020, 6, 1);
    private final LocalDate dateTwo = LocalDate.of(2020, 6, 2);
    private final LocalDate dateThree = LocalDate.of(2020, 6, 15);
    private final Auditorium auditorium = new Auditorium(1L, "one");
    private final Course course = new Course(1L, "one");
    private final Group group = new Group(1L, "one");
    private final Professor professor = new Professor(1L, "one", "one");
    private final ScheduleTemplate templateOne =
            new ScheduleTemplate(1L, false, DayOfWeek.MONDAY, Period.FIRST,
                    auditorium, course, group, professor);
    private final ScheduleTemplate templateTwo =
            new ScheduleTemplate(2L, false, DayOfWeek.TUESDAY, Period.FIRST,
                    auditorium, course, group, professor);
    private final Schedule scheduleOne =
            new Schedule(1L, templateOne, dateOne, DayOfWeek.MONDAY,
                    Period.FIRST, auditorium, course, group, professor);
    private final Schedule scheduleTwo =
            new Schedule(2L, templateTwo, dateTwo, DayOfWeek.TUESDAY,
                    Period.FIRST, auditorium, course, group, professor);
    private final Schedule scheduleThree =
            new Schedule(3L, templateOne, dateThree, DayOfWeek.MONDAY,
                    Period.FIRST, auditorium, course, group, professor);

    @Autowired
    private ScheduleDao repository;

    @Test
    @Sql("classpath:sql/schedule_test.sql")
    public void findAllByDateShouldRetrieveCorrectListOfSchedules() {

        List<Schedule> actual = repository.findAllByDate(dateOne);

        assertThat(actual).containsOnly(scheduleOne)
                .doesNotContain(scheduleTwo, scheduleThree);
    }

    @Test
    @Sql("classpath:sql/schedule_test.sql")
    public void findAllInRangeShouldRetrieveCorrectListOfSchedules() {

        List<Schedule> actual = repository.findAllInRange(dateOne, dateTwo);

        assertThat(actual).containsOnly(scheduleOne, scheduleTwo)
                .doesNotContain(scheduleThree);
    }

    @Test
    @Sql("classpath:sql/schedule_test.sql")
    public void findAllByTemplateIdShouldRetrieveCorrectListOfSchedules() {

        List<Schedule> actual =
                repository.findAllByTemplateId(templateOne.getId());

        assertThat(actual).containsOnly(scheduleOne, scheduleThree)
                .doesNotContain(scheduleTwo);
    }

}