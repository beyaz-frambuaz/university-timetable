package com.shablii.timetable.service.utility.predicates;

import com.shablii.timetable.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SchedulePredicateCourseIdTest {

    private final long id = 1L;
    private SchedulePredicate predicate;

    @BeforeEach
    private void setUp() {

        this.predicate = new SchedulePredicateCourseId(id);
    }

    @Test
    public void testShouldFilterScheduleByCourseId() {

        Course expectedCourse = mock(Course.class);
        Schedule expectedSchedule = mock(Schedule.class);
        given(expectedSchedule.getCourse()).willReturn(expectedCourse);
        given(expectedCourse.getId()).willReturn(id);

        long unexpectedId = 2L;
        Course unexpectedCourse = mock(Course.class);
        Schedule unexpectedSchedule = mock(Schedule.class);
        given(unexpectedSchedule.getCourse()).willReturn(unexpectedCourse);
        given(unexpectedCourse.getId()).willReturn(unexpectedId);

        assertThat(predicate.test(expectedSchedule)).isTrue();
        assertThat(predicate.test(unexpectedSchedule)).isFalse();
    }

    @Test
    public void getCriteriaShouldReturnStringFormattedForLogging() {

        String expected = "course: 1";
        String actual = predicate.getCriteria();
        assertThat(actual).isEqualTo(expected);
    }

}