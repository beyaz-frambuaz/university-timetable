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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
    public void prepareWeekOptionsShouldRequestWeekOptionsFromFacadeAndAssembleWeekOptionsObject() {

        given(timetableFacade.getOptionsForWeek(any(Schedule.class),
                anyInt())).willReturn(Collections.emptyList());

        LocalDate monday = LocalDate.of(2020, 6, 1);
        given(semesterCalendar.getWeekMonday(anyInt())).willReturn(monday);
        String description = "description";
        given(semesterCalendar.getDayDescription(
                any(LocalDate.class))).willReturn(description);
        given(semesterCalendar.getWeekDescription(
                any(LocalDate.class))).willReturn(description);
        String[] shortDescription = { description, description };
        given(semesterCalendar.getDayShortDescription(
                any(LocalDate.class))).willReturn(shortDescription);

        List<DayOptions> dayOptions = new ArrayList<>();
        for (long i = 0; i < 5; i++) {

            dayOptions.add(new DayOptions(Collections.emptyMap(), description,
                    shortDescription, monday.plusDays(i).toString()));
        }

        Schedule schedule = mock(Schedule.class);
        int week = 1;
        WeekOptions expected = new WeekOptions(dayOptions, description, week);

        WeekOptions actual = formatter.prepareWeekOptions(schedule, week);

        assertThat(actual).isEqualTo(expected);
        then(timetableFacade).should().getOptionsForWeek(schedule, week);
    }

    @Test
    public void prepareDayOptionsShouldRequestDayOptionsFromFacadeAndAssembleDayOptionsObject() {

        Auditorium auditorium = mock(Auditorium.class);
        DayOfWeek day = DayOfWeek.MONDAY;
        ReschedulingOption optionOne =
                new ReschedulingOption(1L, day, Period.FIRST, auditorium);
        ReschedulingOption optionTwo =
                new ReschedulingOption(2L, day, Period.SECOND, auditorium);
        given(timetableFacade.getOptionsForDate(any(Schedule.class),
                any(LocalDate.class))).willReturn(
                Arrays.asList(optionOne, optionTwo));

        String description = "description";
        given(semesterCalendar.getDayDescription(
                any(LocalDate.class))).willReturn(description);
        String[] shortDescription = { description, description };
        given(semesterCalendar.getDayShortDescription(
                any(LocalDate.class))).willReturn(shortDescription);

        Map<Period, List<ReschedulingOption>> periodDayOptions =
                new LinkedHashMap<>();
        periodDayOptions.put(Period.FIRST,
                Collections.singletonList(optionOne));
        periodDayOptions.put(Period.SECOND,
                Collections.singletonList(optionTwo));

        LocalDate date = LocalDate.MAX;
        DayOptions expected =
                new DayOptions(periodDayOptions, description, shortDescription,
                        date.toString());

        Schedule schedule = mock(Schedule.class);

        DayOptions actual = formatter.prepareDayOptions(schedule, date);

        assertThat(actual).isEqualTo(expected);
        then(timetableFacade).should().getOptionsForDate(schedule, date);
    }

}