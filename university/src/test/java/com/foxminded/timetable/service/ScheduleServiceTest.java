package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateGroupId;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    private final Long                  id         = 1L;
    private final Long                  templateId = 1L;
    private final LocalDate             date       = LocalDate.of(2020, 1, 1);
    private final DayOfWeek             day        = DayOfWeek.MONDAY;
    private final Period                period     = Period.FIRST;
    private final Auditorium            auditorium = new Auditorium(1L, "A-01");
    private final Course                course     = new Course(1L, "course");
    private final Group                 group      = new Group(1L, "G-01");
    private final Professor             professor  = new Professor(1L, "one",
            "one");
    private final Schedule              schedule   = new Schedule(id,
            templateId, date, day, period, auditorium, course, group,
            professor);
    @Mock
    private ScheduleDao         repository;
    @Mock
    private SemesterCalendar    semesterCalendar;
    @Mock
    private ScheduleTemplateDao templateRepository;
    @InjectMocks
    private       ScheduleService       service;

    @Test
    public void countShouldDelegateToRepository() {

        given(repository.count()).willReturn(id);

        long actual = repository.count();

        then(repository).should().count();
        assertThat(actual).isEqualTo(id);

    }

    @Test
    public void saveShouldAddScheduleToRepositoryIfNew() {

        Schedule expected = new Schedule(null, templateId, date, day, period,
                auditorium, course, group, professor);
        given(repository.save(any(Schedule.class))).willReturn(expected);

        Schedule actual = service.save(expected);

        then(repository).should().save(expected);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveShouldUpdateScheduleInRepositoryIfExisting() {

        given(repository.update(any(Schedule.class))).willReturn(schedule);

        Schedule actual = service.save(schedule);

        then(repository).should().update(schedule);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).isEqualTo(schedule);
    }

    @Test
    public void saveAllShouldDelegateToRepository() {

        List<Schedule> schedules = Collections.singletonList(schedule);
        given(repository.saveAll(anyList())).willReturn(schedules);

        List<Schedule> actual = service.saveAll(schedules);

        then(repository).should().saveAll(schedules);
        assertThat(actual).hasSameElementsAs(schedules);
    }

    @Test
    public void saveAllShouldReturnListBackGivenEmptyList() {

        List<Schedule> expected = Collections.emptyList();

        List<Schedule> actual = service.saveAll(expected);

        then(repository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void updateAllShouldCalculateDateShiftAndUpdateAllWithTemplateIdInRepository() {

        LocalDate newDate = LocalDate.of(2020, 1, 5);
        int deltaDays = 4;

        service.updateAll(schedule, newDate);

        then(repository).should().updateAllWithTemplateId(schedule, deltaDays);
    }

    @Test
    public void findByIdShouldDelegateToRepository() {

        Optional<Schedule> expected = Optional.of(schedule);
        given(repository.findById(anyLong())).willReturn(expected);

        Optional<Schedule> actual = service.findById(id);

        then(repository).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAllShouldDelegateToRepository() {

        List<Schedule> schedules = Collections.singletonList(schedule);
        given(repository.findAll()).willReturn(schedules);

        List<Schedule> actual = service.findAll();

        then(repository).should().findAll();
        assertThat(actual).isEqualTo(schedules);
    }

    @Test
    public void findAllForShouldFindAllInRangeAndFilterByGivenPredicate() {

        Schedule expected = mock(Schedule.class);
        SchedulePredicate predicate = mock(SchedulePredicateGroupId.class);
        given(predicate.test(expected)).willReturn(true);
        given(predicate.test(schedule)).willReturn(false);
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        List<Schedule> unfiltered = Arrays.asList(schedule, expected);
        given(repository.findAllByDate(any(LocalDate.class))).willReturn(
                unfiltered);

        List<Schedule> actual = service.findAllFor(predicate, date, date);

        assertThat(actual).containsOnly(expected).doesNotContain(schedule);
    }

    @Test
    public void findAllInRangeShouldRequestSchedulesFromRepositoryForEachSemesterDateInRange() {

        LocalDate startDate = date;
        given(semesterCalendar.isSemesterDate(startDate)).willReturn(true);
        LocalDate middleDate = LocalDate.of(2020, 1, 2);
        given(semesterCalendar.isSemesterDate(middleDate)).willReturn(false);
        LocalDate endDate = LocalDate.of(2020, 1, 3);
        given(semesterCalendar.isSemesterDate(endDate)).willReturn(true);

        Schedule dateSchedule = mock(Schedule.class);
        List<Schedule> dayOneSchedules = Collections.singletonList(
                dateSchedule);
        given(repository.findAllByDate(startDate)).willReturn(dayOneSchedules);

        Schedule endDateSchedule = mock(Schedule.class);
        List<Schedule> dayTwoSchedules = Collections.singletonList(
                endDateSchedule);
        given(repository.findAllByDate(endDate)).willReturn(dayTwoSchedules);

        List<Schedule> actual = service.findAllInRange(startDate, endDate);

        then(repository).should().findAllByDate(startDate);
        then(repository).should().findAllByDate(endDate);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).containsExactly(dateSchedule, endDateSchedule);
    }

    @Test
    public void findAllByTemplateIdShouldDelegateToRepository() {

        List<Schedule> schedules = Collections.singletonList(schedule);
        given(repository.findAllByTemplateId(anyLong())).willReturn(schedules);

        List<Schedule> actual = service.findAllByTemplateId(templateId);

        then(repository).should().findAllByTemplateId(templateId);
        assertThat(actual).isEqualTo(schedules);
    }

    @Test
    public void findAllInRangeShouldGenerateAndSaveSchedulesWhenNoneFoundInRepository() {

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate until = LocalDate.of(2020, 1, 3);

        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);
        given(repository.findAllByDate(any(LocalDate.class))).willReturn(
                Collections.emptyList());
        given(templateRepository.findAllByDate(anyBoolean(),
                any(DayOfWeek.class))).willReturn(Collections.emptyList());
        given(repository.saveAll(anyList())).willReturn(
                Collections.emptyList());

        service.findAllInRange(from, until);

        then(repository).should(atLeastOnce())
                .findAllByDate(any(LocalDate.class));
        then(templateRepository).should(atLeastOnce())
                .findAllByDate(anyBoolean(), any(DayOfWeek.class));
        then(repository).should(atLeastOnce()).saveAll(anyList());
    }

}
