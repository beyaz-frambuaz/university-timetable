package com.foxminded.timetable.service.utility.predicates;

import com.foxminded.timetable.model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SchedulePredicateGroupIdTest {

    private final long id = 1L;
    private SchedulePredicate predicate;

    @BeforeEach
    private void setUp() {

        this.predicate = new SchedulePredicateGroupId(id);
    }

    @Test
    public void testShouldFilterScheduleByGroupId() {

        Group expectedGroup = mock(Group.class);
        Schedule expectedSchedule = mock(Schedule.class);
        given(expectedSchedule.getGroup()).willReturn(expectedGroup);
        given(expectedGroup.getId()).willReturn(id);

        long unexpectedId = 2L;
        Group unexpectedGroup = mock(Group.class);
        Schedule unexpectedSchedule = mock(Schedule.class);
        given(unexpectedSchedule.getGroup()).willReturn(unexpectedGroup);
        given(unexpectedGroup.getId()).willReturn(unexpectedId);

        assertThat(predicate.test(expectedSchedule)).isTrue();
        assertThat(predicate.test(unexpectedSchedule)).isFalse();
    }

    @Test
    public void getCriteriaShouldReturnStringFormattedForLogging() {

        String expected = "group: 1";
        String actual = predicate.getCriteria();
        assertThat(actual).isEqualTo(expected);
    }

}