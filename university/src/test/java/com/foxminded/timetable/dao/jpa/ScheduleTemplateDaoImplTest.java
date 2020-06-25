package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.*;
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
class ScheduleTemplateDaoImplTest {

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
    private final ScheduleTemplate templateThree =
            new ScheduleTemplate(3L, true, DayOfWeek.MONDAY, Period.FIRST,
                    auditorium, course, group, professor);

    @Autowired
    private ScheduleTemplateDao repository;

    @Test
    @Sql("classpath:sql/schedule_template_test.sql")
    public void findAllByWeekShouldRetrieveCorrectListOfTemplates() {

        List<ScheduleTemplate> actual = repository.findAllByWeek(false);

        assertThat(actual).containsOnly(templateOne, templateTwo)
                .doesNotContain(templateThree);
    }

    @Test
    @Sql("classpath:sql/schedule_template_test.sql")
    public void findAllByDateShouldRetrieveCorrectListOfTemplates() {

        List<ScheduleTemplate> actual =
                repository.findAllByDay(false, DayOfWeek.MONDAY);

        assertThat(actual).containsOnly(templateOne)
                .doesNotContain(templateTwo, templateThree);
    }

}