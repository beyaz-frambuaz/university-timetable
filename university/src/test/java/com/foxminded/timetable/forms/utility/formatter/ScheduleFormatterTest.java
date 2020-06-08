package com.foxminded.timetable.forms.utility.formatter;

import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.TwoWeekSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateNoFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleFormatterTest {

    private final SchedulePredicate predicate = new SchedulePredicateNoFilter();
    private final LocalDate         start     = LocalDate.of(2020, 1, 1);
    private final LocalDate         end       = LocalDate.of(2020, 1, 2);
    private final boolean           filtered  = false;
    @Mock
    private       TimetableFacade   timetableFacade;
    @Mock
    private       SemesterCalendar  semesterCalendar;
    @InjectMocks
    private       ScheduleFormatter formatter;

    @Test
    public void prepareDayScheduleShouldRequestFilteredSchedulesFromServiceWhenFilteredIsTrue() {

        Schedule schedule = mock(Schedule.class);
        given(schedule.getPeriod()).willReturn(Period.FIRST);
        List<Schedule> schedules = Collections.singletonList(schedule);
        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                schedules);

        formatter.prepareDaySchedule(predicate, start, true);

        then(timetableFacade).should().getScheduleFor(predicate, start, start);
        then(timetableFacade).should(never()).getScheduleInRange(start, start);
    }

    @Test
    public void prepareDayScheduleShouldRequestSchedulesFromServiceAndAssembleDayScheduleObject() {

        String description = "test";
        String[] shortDescription = { "test", "test" };
        given(semesterCalendar.getDayDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getDayShortDescription(
                any(LocalDate.class))).willReturn(shortDescription);
        Period firstPeriod = Period.FIRST;
        Period secondPeriod = Period.SECOND;

        Schedule scheduleFirst = mock(Schedule.class);
        given(scheduleFirst.getPeriod()).willReturn(firstPeriod);

        Schedule scheduleSecond = mock(Schedule.class);
        given(scheduleSecond.getPeriod()).willReturn(secondPeriod);

        List<Schedule> schedules = Arrays.asList(scheduleFirst, scheduleSecond);
        given(timetableFacade.getScheduleInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(schedules);

        Map<Period, List<Schedule>> periodSchedules = new HashMap<>();
        periodSchedules.put(firstPeriod,
                Collections.singletonList(scheduleFirst));
        periodSchedules.put(secondPeriod,
                Collections.singletonList(scheduleSecond));

        DaySchedule expected = new DaySchedule(periodSchedules, description,
                shortDescription);

        DaySchedule actual = formatter.prepareDaySchedule(predicate, start,
                filtered);

        then(timetableFacade).should().getScheduleInRange(start, start);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void prepareWeekScheduleShouldAssembleDaySchedulesIntoWeekScheduleObject() {

        int weekNumber = 1;
        String description = "test";
        String[] shortDescription = { "test", "test" };
        given(semesterCalendar.getWeekMonday(any(LocalDate.class))).willReturn(
                start);
        given(semesterCalendar.getWeekFriday(any(LocalDate.class))).willReturn(
                end);
        given(semesterCalendar.getDayDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getDayShortDescription(
                any(LocalDate.class))).willReturn(shortDescription);
        given(semesterCalendar.getWeekDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getSemesterWeekNumber(
                any(LocalDate.class))).willReturn(weekNumber);
        Period firstPeriod = Period.FIRST;
        Period secondPeriod = Period.SECOND;

        Schedule scheduleStart = mock(Schedule.class);
        given(scheduleStart.getPeriod()).willReturn(firstPeriod);

        Schedule scheduleEnd = mock(Schedule.class);
        given(scheduleEnd.getPeriod()).willReturn(secondPeriod);

        given(timetableFacade.getScheduleInRange(start, start)).willReturn(
                Collections.singletonList(scheduleStart));
        given(timetableFacade.getScheduleInRange(end, end)).willReturn(
                Collections.singletonList(scheduleEnd));

        Map<Period, List<Schedule>> periodSchedulesStart = new HashMap<>();
        periodSchedulesStart.put(firstPeriod,
                Collections.singletonList(scheduleStart));
        DaySchedule dayScheduleStart = new DaySchedule(periodSchedulesStart,
                description, shortDescription);
        Map<Period, List<Schedule>> periodSchedulesEnd = new HashMap<>();
        periodSchedulesEnd.put(secondPeriod,
                Collections.singletonList(scheduleEnd));
        DaySchedule dayScheduleEnd = new DaySchedule(periodSchedulesEnd,
                description, shortDescription);
        List<DaySchedule> daySchedules = Arrays.asList(dayScheduleStart,
                dayScheduleEnd);

        WeekSchedule expected = new WeekSchedule(daySchedules, description,
                weekNumber);

        WeekSchedule actual = formatter.prepareWeekSchedule(predicate, start,
                filtered);

        then(timetableFacade).should().getScheduleInRange(start, start);
        then(timetableFacade).should().getScheduleInRange(end, end);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void prepareMonthScheduleShouldAssembleDayAndWeekSchedulesIntoMonthScheduleObject() {

        int weekNumber = 1;
        String description = "test";
        String[] shortDescription = { "test", "test" };
        given(semesterCalendar.getMonthDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getFirstSemesterDayOfMonth(any(LocalDate.class)))
                .willReturn(start);
        given(semesterCalendar.getLastSemesterDayOfMonth(
                any(LocalDate.class))).willReturn(start);
        given(semesterCalendar.getWeekMonday(any(LocalDate.class))).willReturn(
                start);
        given(semesterCalendar.getWeekMonday(anyInt())).willReturn(start);
        given(semesterCalendar.getWeekFriday(any(LocalDate.class))).willReturn(
                end);
        given(semesterCalendar.getDayDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getDayShortDescription(
                any(LocalDate.class))).willReturn(shortDescription);
        given(semesterCalendar.getWeekDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getSemesterWeekNumber(
                any(LocalDate.class))).willReturn(weekNumber);
        Period firstPeriod = Period.FIRST;
        Period secondPeriod = Period.SECOND;

        Schedule scheduleStart = mock(Schedule.class);
        given(scheduleStart.getPeriod()).willReturn(firstPeriod);

        Schedule scheduleEnd = mock(Schedule.class);
        given(scheduleEnd.getPeriod()).willReturn(secondPeriod);

        given(timetableFacade.getScheduleInRange(start, start)).willReturn(
                Collections.singletonList(scheduleStart));
        given(timetableFacade.getScheduleInRange(end, end)).willReturn(
                Collections.singletonList(scheduleEnd));

        Map<Period, List<Schedule>> periodSchedulesStart = new HashMap<>();
        periodSchedulesStart.put(firstPeriod,
                Collections.singletonList(scheduleStart));
        DaySchedule dayScheduleStart = new DaySchedule(periodSchedulesStart,
                description, shortDescription);
        Map<Period, List<Schedule>> periodSchedulesEnd = new HashMap<>();
        periodSchedulesEnd.put(secondPeriod,
                Collections.singletonList(scheduleEnd));
        DaySchedule dayScheduleEnd = new DaySchedule(periodSchedulesEnd,
                description, shortDescription);
        List<DaySchedule> daySchedules = Arrays.asList(dayScheduleStart,
                dayScheduleEnd);

        WeekSchedule weekSchedule = new WeekSchedule(daySchedules, description,
                weekNumber);
        MonthSchedule expected = new MonthSchedule(
                Collections.singletonList(weekSchedule), description);

        MonthSchedule actual = formatter.prepareMonthSchedule(predicate, start,
                filtered);

        then(timetableFacade).should().getScheduleInRange(start, start);
        then(timetableFacade).should().getScheduleInRange(end, end);
        assertThat(actual).isEqualTo(expected);

    }

    @Test
    public void prepareTwoWeekScheduleShouldRequestTemplatesFromServiceAndReturnTwoWeekObject() {

        ScheduleTemplate oddTemplate = mock(ScheduleTemplate.class);
        ScheduleTemplate evenTemplate = mock(ScheduleTemplate.class);
        given(timetableFacade.getTwoWeekSchedule()).willReturn(
                Arrays.asList(oddTemplate, evenTemplate));
        Period firstPeriod = Period.FIRST;
        Period secondPeriod = Period.SECOND;
        given(oddTemplate.getWeekParity()).willReturn(false);
        given(oddTemplate.getPeriod()).willReturn(firstPeriod);
        given(evenTemplate.getWeekParity()).willReturn(true);
        given(evenTemplate.getPeriod()).willReturn(secondPeriod);
        Map<Period, List<ScheduleTemplate>> oddWeek = Collections.singletonMap(
                firstPeriod, Collections.singletonList(oddTemplate));
        Map<Period, List<ScheduleTemplate>> evenWeek = Collections.singletonMap(
                secondPeriod, Collections.singletonList(evenTemplate));
        TwoWeekSchedule expected = new TwoWeekSchedule(oddWeek, evenWeek);

        TwoWeekSchedule actual = formatter.prepareTwoWeekSchedule();

        then(timetableFacade).should().getTwoWeekSchedule();
        assertThat(actual).isEqualTo(expected);
    }

}