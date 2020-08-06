package com.foxminded.timetable.service.utility.predicates;

import com.foxminded.timetable.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SchedulePredicateProfessorIdTest {

    private final long id = 1L;
    private SchedulePredicate predicate;

    @BeforeEach
    private void setUp() {

        this.predicate = new SchedulePredicateProfessorId(id);
    }

    @Test
    public void testShouldFilterScheduleByProfessorId() {

        Professor expectedProfessor = mock(Professor.class);
        Schedule expectedSchedule = mock(Schedule.class);
        given(expectedSchedule.getProfessor()).willReturn(expectedProfessor);
        given(expectedProfessor.getId()).willReturn(id);

        long unexpectedId = 2L;
        Professor unexpectedProfessor = mock(Professor.class);
        Schedule unexpectedSchedule = mock(Schedule.class);
        given(unexpectedSchedule.getProfessor()).willReturn(
                unexpectedProfessor);
        given(unexpectedProfessor.getId()).willReturn(unexpectedId);

        assertThat(predicate.test(expectedSchedule)).isTrue();
        assertThat(predicate.test(unexpectedSchedule)).isFalse();
    }

    @Test
    public void getCriteriaShouldReturnStringFormattedForLogging() {

        String expected = "professor: 1";
        String actual = predicate.getCriteria();
        assertThat(actual).isEqualTo(expected);
    }

}