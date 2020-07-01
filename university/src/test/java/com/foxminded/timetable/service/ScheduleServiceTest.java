package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ScheduleRepository;
import com.foxminded.timetable.dao.ScheduleTemplateRepository;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateGroupId;
import org.junit.jupiter.api.BeforeEach;
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

    private final Long             id         = 1L;
    private final LocalDate        date       = LocalDate.of(2020, 6, 1);
    private final DayOfWeek        day        = DayOfWeek.MONDAY;
    private final Period           period     = Period.FIRST;
    private final Auditorium       auditorium = new Auditorium(1L, "A-01");
    private final Course           course     = new Course(1L, "course");
    private final Group            group      = new Group(1L, "G-01");
    private final Professor        professor  = new Professor(1L, "one", "one");
    private       Schedule         schedule;

    @Mock
    private ScheduleRepository repository;
    @Mock
    private SemesterCalendar    semesterCalendar;
    @Mock
    private ScheduleTemplateRepository templateRepository;
    @InjectMocks
    private ScheduleService     service;

    @BeforeEach
    private void createSchedule() {

        ScheduleTemplate template =
                new ScheduleTemplate(id, false, day, period, auditorium, course,
                        group, professor);
        this.schedule = new Schedule(template, date);
        this.schedule.setId(id);
    }

    @Test
    public void saveShouldDelegateToRepository() {

        given(repository.save(any(Schedule.class))).willReturn(schedule);

        Schedule actual = service.save(schedule);

        then(repository).should().save(schedule);
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
    public void updateAllWithSameTemplateIdShouldRequestAllByTemplateIdFromRepositoryAndUpdateEach() {

        Schedule candidate = new Schedule(schedule);
        long dateShift = 3L;
        LocalDate targetDate = date.plusDays(dateShift);
        DayOfWeek newDay = targetDate.getDayOfWeek();
        Period newPeriod = Period.FIFTH;
        Auditorium newAuditorium = new Auditorium("new");
        candidate.setDay(newDay);
        candidate.setPeriod(newPeriod);
        candidate.setAuditorium(newAuditorium);

        Schedule scheduleOne = schedule;
        Schedule scheduleTwo = new Schedule(schedule);
        scheduleTwo.setDate(LocalDate.of(2020, 6, 15));
        List<Schedule> beforeUpdate = Arrays.asList(scheduleOne, scheduleTwo);
        given(repository.findAllByTemplateId(anyLong())).willReturn(
                beforeUpdate);

        Schedule updatedOne = new Schedule(scheduleOne);
        updatedOne.setDate(updatedOne.getDate().plusDays(dateShift));
        updatedOne.setDay(newDay);
        updatedOne.setPeriod(newPeriod);
        updatedOne.setAuditorium(newAuditorium);

        Schedule updatedTwo = new Schedule(scheduleTwo);
        updatedTwo.setDate(updatedTwo.getDate().plusDays(dateShift));
        updatedTwo.setDay(newDay);
        updatedTwo.setPeriod(newPeriod);
        updatedTwo.setAuditorium(newAuditorium);

        List<Schedule> expected = Arrays.asList(updatedOne, updatedTwo);

        List<Schedule> actual =
                service.updateAllWithSameTemplateId(candidate, targetDate);

        assertThat(actual).isEqualTo(expected);
        then(repository).should().findAllByTemplateId(id);
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
    public void findAllGeneratedInRangeShouldDelegateToRepository() {

        List<Schedule> expected = Collections.singletonList(schedule);
        given(repository.findAllByDateBetween(any(LocalDate.class),
                any(LocalDate.class))).willReturn(expected);

        List<Schedule> actual = service.findGeneratedInRange(date, date);

        then(repository).should().findAllByDateBetween(date, date);
        assertThat(actual).isEqualTo(expected);
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
        LocalDate middleDate = LocalDate.of(2020, 6, 2);
        given(semesterCalendar.isSemesterDate(middleDate)).willReturn(false);
        LocalDate endDate = LocalDate.of(2020, 6, 3);
        given(semesterCalendar.isSemesterDate(endDate)).willReturn(true);

        Schedule startDateSchedule = mock(Schedule.class);
        List<Schedule> dayOneSchedules =
                Collections.singletonList(startDateSchedule);
        given(repository.findAllByDate(startDate)).willReturn(dayOneSchedules);

        Schedule endDateSchedule = mock(Schedule.class);
        List<Schedule> dayTwoSchedules =
                Collections.singletonList(endDateSchedule);
        given(repository.findAllByDate(endDate)).willReturn(dayTwoSchedules);

        List<Schedule> actual = service.findAllInRange(startDate, endDate);

        then(repository).should().findAllByDate(startDate);
        then(repository).should().findAllByDate(endDate);
        then(repository).shouldHaveNoMoreInteractions();
        assertThat(actual).containsExactly(startDateSchedule, endDateSchedule);
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
        given(templateRepository.findAllByWeekParityAndDay(anyBoolean(),
                any(DayOfWeek.class))).willReturn(Collections.emptyList());
        given(repository.saveAll(anyList())).willReturn(
                Collections.emptyList());

        service.findAllInRange(from, until);

        then(repository).should(atLeastOnce())
                .findAllByDate(any(LocalDate.class));
        then(templateRepository).should(atLeastOnce())
                .findAllByWeekParityAndDay(anyBoolean(), any(DayOfWeek.class));
        then(repository).should(atLeastOnce()).saveAll(anyList());
    }

    @Test
    public void deleteAllShouldDelegateToRepository() {

        service.deleteAll();

        then(repository).should().deleteAllInBatch();
    }

}
