package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.model.Schedule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SchedulePredicateNoFilterTest {

    private SchedulePredicate predicate;

    @BeforeEach
    private void setUp() {

        this.predicate = new SchedulePredicateNoFilter();
    }

    @Test
    public void testShouldNotFilterSchedule() {

        Schedule scheduleOne = mock(Schedule.class);
        Schedule scheduleTwo = mock(Schedule.class);

        assertThat(predicate.test(scheduleOne)).isTrue();
        assertThat(predicate.test(scheduleTwo)).isTrue();
    }

    @Test
    public void getCriteriaShouldReturnStringFormattedForLogging() {

        String expected = "no filter";
        String actual = predicate.getCriteria();
        assertThat(actual).isEqualTo(expected);
    }

}