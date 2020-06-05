package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ReschedulingOptionServiceTest {

    private final ReschedulingOption        option =
            new ReschedulingOption(1L, DayOfWeek.MONDAY, Period.FIRST,
                    new Auditorium("A-01"));
    @Mock
    private       ReschedulingOptionDao     repository;
    @Mock
    private       SemesterCalendar          semesterCalendar;
    @InjectMocks
    private       ReschedulingOptionService service;

    @Test
    public void countShouldDelegateToRepository() {

        long expected = 1L;
        given(repository.count()).willReturn(expected);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<ReschedulingOption> options = Collections.singletonList(option);
        given(repository.saveAll(anyList())).willReturn(options);

        List<ReschedulingOption> actual = service.saveAll(options);

        then(repository).should().saveAll(options);
        assertThat(actual).hasSameElementsAs(options);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<ReschedulingOption> expected = Collections.emptyList();

        List<ReschedulingOption> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<ReschedulingOption> options = Collections.singletonList(option);
        given(repository.findAll()).willReturn(options);

        List<ReschedulingOption> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(options);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        long id = 1L;
        Optional<ReschedulingOption> expected = Optional.of(option);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<ReschedulingOption> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllForShouldMapRepositoryOptionsToEachSemesterDateInRange() {

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate until = LocalDate.of(2020, 1, 3);
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);
        Schedule schedule = mock(Schedule.class);
        List<ReschedulingOption> optionsOne = Collections.emptyList();
        given(repository.findDayReschedulingOptionsForSchedule(false, from,
                schedule)).willReturn(optionsOne);
        List<ReschedulingOption> optionsTwo = Collections.emptyList();
        given(repository.findDayReschedulingOptionsForSchedule(false,
                from.plusDays(1L), schedule)).willReturn(optionsTwo);
        List<ReschedulingOption> optionsThree = Collections.emptyList();
        given(repository.findDayReschedulingOptionsForSchedule(false,
                from.plusDays(2L), schedule)).willReturn(optionsThree);

        Map<LocalDate, List<ReschedulingOption>> actual =
                service.findAllFor(schedule, from, until);

        then(repository).should()
                .findDayReschedulingOptionsForSchedule(false, from, schedule);
        then(repository).should()
                .findDayReschedulingOptionsForSchedule(false, from.plusDays(1L),
                        schedule);
        then(repository).should()
                .findDayReschedulingOptionsForSchedule(false, until, schedule);
        assertThat(actual).containsKeys(from, from.plusDays(1L), until)
                .containsValues(optionsOne, optionsTwo, optionsThree);
    }

    @Test
    public void findAllForShouldNotRequestOptionsFromRepositoryForNonSemesterDates() {

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate until = LocalDate.of(2020, 1, 3);
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                false);
        Schedule schedule = mock(Schedule.class);

        Map<LocalDate, List<ReschedulingOption>> actual =
                service.findAllFor(schedule, from, until);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

}
