package com.foxminded.timetable.forms.utility.formatter;

import com.foxminded.timetable.forms.utility.DayOptions;
import com.foxminded.timetable.forms.utility.WeekOptions;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.SemesterCalendar;
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
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class OptionsFormatterTest {

    @Mock
    private TimetableFacade  timetableFacade;
    @Mock
    private SemesterCalendar semesterCalendar;

    @InjectMocks
    private OptionsFormatter formatter;

    @Test
    public void prepareWeekOptionsShouldRequestOptionsFromFacadeAndAssembleWeekOptionsObject() {

        Schedule schedule = mock(Schedule.class);
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.of(2020, 1, 2);
        String description = "test";
        String[] shortDayDescription = { "test", "test" };
        int weekNumber = 1;
        Period firstPeriod = Period.FIRST;
        Period secondPeriod = Period.SECOND;
        Auditorium auditorium = new Auditorium("A-01");
        ReschedulingOption optionOne = new ReschedulingOption(1L,
                start.getDayOfWeek(), firstPeriod, auditorium);
        ReschedulingOption optionTwo = new ReschedulingOption(2L,
                end.getDayOfWeek(), secondPeriod, auditorium);
        List<ReschedulingOption> startOptions = Arrays.asList(optionOne,
                optionTwo);
        List<ReschedulingOption> endOptions = Collections.singletonList(
                optionOne);
        Map<LocalDate, List<ReschedulingOption>> options = new HashMap<>();
        options.put(start, startOptions);
        options.put(end, endOptions);

        Map<Period, List<ReschedulingOption>> startPeriodOptions =
                new HashMap<>();
        startPeriodOptions.put(firstPeriod,
                Collections.singletonList(optionOne));
        startPeriodOptions.put(secondPeriod,
                Collections.singletonList(optionTwo));
        Map<Period, List<ReschedulingOption>> endPeriodOptions =
                new HashMap<>();
        endPeriodOptions.put(firstPeriod, Collections.singletonList(optionOne));
        DayOptions startDayOptions = new DayOptions(startPeriodOptions,
                description, shortDayDescription, start.toString());
        DayOptions endDayOptions = new DayOptions(endPeriodOptions, description,
                shortDayDescription, end.toString());
        WeekOptions expected = new WeekOptions(
                Arrays.asList(startDayOptions, endDayOptions), description,
                weekNumber);

        given(timetableFacade.getOptionsFor(any(Schedule.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                options);
        given(semesterCalendar.getWeekDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getSemesterWeekNumber(
                any(LocalDate.class))).willReturn(weekNumber);
        given(semesterCalendar.getDayDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getDayShortDescription(
                any(LocalDate.class))).willReturn(shortDayDescription);

        WeekOptions actual = formatter.prepareWeekOptions(schedule, start, end);

        assertThat(actual).isEqualTo(expected);
        then(timetableFacade).should().getOptionsFor(schedule, start, end);
    }

}