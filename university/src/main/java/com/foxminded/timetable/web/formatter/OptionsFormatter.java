package com.foxminded.timetable.web.formatter;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.SemesterCalendar;
import com.foxminded.timetable.service.TimetableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OptionsFormatter {

    private final SemesterCalendar semesterCalendar;
    private final TimetableService timetableService;

    public WeekOptions prepareWeekOptions(Schedule schedule, LocalDate monday,
            LocalDate friday) {

        Map<LocalDate, List<ReschedulingOption>> optionsFor =
                timetableService.getReschedulingOptionsFor(schedule, monday,
                        friday);
        List<DayOptions> dayOptions = new ArrayList<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(monday, friday) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {

            DayOptions options = prepareDayOptions(monday.plusDays(i),
                    optionsFor.get(monday.plusDays(i)));
            dayOptions.add(options);
        }
        String weekDescription = semesterCalendar.getWeekDescription(monday);
        int weekNumber = semesterCalendar.getSemesterWeekNumber(monday);

        return new WeekOptions(dayOptions, weekDescription, weekNumber);
    }

    public DayOptions prepareDayOptions(LocalDate date,
            List<ReschedulingOption> options) {

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
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
    }

}
