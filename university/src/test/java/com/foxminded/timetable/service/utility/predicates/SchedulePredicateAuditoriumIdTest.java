package com.foxminded.timetable.service.utility.predicates;

import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Schedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SchedulePredicateAuditoriumIdTest {

    private final long              id = 1L;
    private       SchedulePredicate predicate;

    @BeforeEach
    private void setUp() {

        this.predicate = new SchedulePredicateAuditoriumId(id);
    }

    @Test
    public void testShouldFilterScheduleByAuditoriumId() {

        Auditorium expectedAuditorium = mock(Auditorium.class);
        Schedule expectedSchedule = mock(Schedule.class);
        given(expectedSchedule.getAuditorium()).willReturn(expectedAuditorium);
        given(expectedAuditorium.getId()).willReturn(id);

        long unexpectedId = 2L;
        Auditorium unexpectedAuditorium = mock(Auditorium.class);
        Schedule unexpectedSchedule = mock(Schedule.class);
        given(unexpectedSchedule.getAuditorium()).willReturn(
                unexpectedAuditorium);
        given(unexpectedAuditorium.getId()).willReturn(unexpectedId);

        assertThat(predicate.test(expectedSchedule)).isTrue();
        assertThat(predicate.test(unexpectedSchedule)).isFalse();
    }

    @Test
    public void getCriteriaShouldReturnStringFormattedForLogging() {

        String expected = "auditorium: 1";
        String actual = predicate.getCriteria();
        assertThat(actual).isEqualTo(expected);
    }

}