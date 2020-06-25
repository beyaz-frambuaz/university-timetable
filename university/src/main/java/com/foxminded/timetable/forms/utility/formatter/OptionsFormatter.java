package com.foxminded.timetable.forms.utility.formatter;

import com.foxminded.timetable.forms.utility.DayOptions;
import com.foxminded.timetable.forms.utility.WeekOptions;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptionsFormatter {

    private final SemesterCalendar semesterCalendar;
    private final TimetableFacade timetableFacade;

    public WeekOptions prepareWeekOptions(Schedule schedule, int weekNumber) {

        List<ReschedulingOption> options =
                timetableFacade.getOptionsForWeek(schedule, weekNumber);

        List<DayOptions> dayOptions = new ArrayList<>();
        LocalDate monday = semesterCalendar.getWeekMonday(weekNumber);
        for (long i = 0; i < 5; i++) {

            LocalDate weekDate = monday.plusDays(i);
            DayOptions dayOption = convertToDayOptions(options.stream()
                    .filter(option -> option.getDay()
                            == weekDate.getDayOfWeek())
                    .collect(Collectors.toList()), weekDate);
            dayOptions.add(dayOption);
        }
        String weekDescription = semesterCalendar.getWeekDescription(monday);

        return new WeekOptions(dayOptions, weekDescription, weekNumber);
    }

    public DayOptions prepareDayOptions(Schedule schedule, LocalDate date) {

        List<ReschedulingOption> options =
                timetableFacade.getOptionsForDate(schedule, date);

        return convertToDayOptions(options, date);
    }

    private DayOptions convertToDayOptions(List<ReschedulingOption> options,
            LocalDate date) {

        String dayDescription = semesterCalendar.getDayDescription(date);
        String[] dayShortDescription =
                semesterCalendar.getDayShortDescription(date);

        Map<Period, List<ReschedulingOption>> periodDayOptions =
                convertToPeriodDayOptions(options);

        return new DayOptions(periodDayOptions, dayDescription,
                dayShortDescription, date.toString());
    }

    private Map<Period, List<ReschedulingOption>> convertToPeriodDayOptions(
            List<ReschedulingOption> options) {

        return options.stream()
                .sorted()
                .collect(Collectors.groupingBy(ReschedulingOption::getPeriod,
                        LinkedHashMap::new, Collectors.toList()));
    }

}
