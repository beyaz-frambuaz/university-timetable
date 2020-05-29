package com.foxminded.timetable.web.formatter;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.SemesterCalendar;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.filter.SchedulePredicate;
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
public class ScheduleFormatter {

    private final SemesterCalendar semesterCalendar;
    private final TimetableService timetableService;

    public DaySchedule prepareDaySchedule(SchedulePredicate predicate,
            LocalDate date, boolean filtered) {

        String dayDescription = semesterCalendar.getDayDescription(date);
        String[] dayShortDescription = semesterCalendar.getDayShortDescription(
                date);

        List<Schedule> schedule;
        if (filtered) {
            schedule = timetableService.getScheduleFor(predicate, date, date);
        } else {
            schedule = timetableService.getScheduleInRange(date, date);
        }
        Map<Period, List<Schedule>> periodDaySchedule =
                convertToPeriodDaySchedule(
                date, schedule);

        return new DaySchedule(periodDaySchedule, dayDescription,
                dayShortDescription);
    }

    public WeekSchedule prepareWeekSchedule(SchedulePredicate predicate,
            LocalDate anyWeekDate, boolean filtered) {

        LocalDate monday = semesterCalendar.getWeekMonday(anyWeekDate);
        LocalDate friday = semesterCalendar.getWeekFriday(anyWeekDate);

        List<DaySchedule> daySchedules = new ArrayList<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(monday, friday) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {

            DaySchedule daySchedule = prepareDaySchedule(predicate,
                    monday.plusDays(i), filtered);
            daySchedules.add(daySchedule);
        }
        String weekDescription = semesterCalendar.getWeekDescription(
                anyWeekDate);
        int weekNumber = semesterCalendar.getSemesterWeekNumber(anyWeekDate);

        return new WeekSchedule(daySchedules, weekDescription, weekNumber);
    }

    public MonthSchedule prepareMonthSchedule(SchedulePredicate predicate,
            LocalDate anyMonthDate, boolean filtered) {

        String monthDescription = semesterCalendar.getMonthDescription(
                anyMonthDate);

        LocalDate firstOfMonth = semesterCalendar.getFirstSemesterDayOfMonth(
                anyMonthDate);
        LocalDate lastOfMonth = semesterCalendar.getLastSemesterDayOfMonth(
                anyMonthDate);
        List<WeekSchedule> weekSchedules = new ArrayList<>();
        int weekNumber = semesterCalendar.getSemesterWeekNumber(firstOfMonth);
        int lastMonthWeek = semesterCalendar.getSemesterWeekNumber(lastOfMonth);
        for (; weekNumber <= lastMonthWeek; weekNumber++) {

            LocalDate monday = semesterCalendar.getWeekMonday(weekNumber);
            WeekSchedule weekSchedule = prepareWeekSchedule(predicate, monday,
                    filtered);
            weekSchedules.add(weekSchedule);
        }

        return new MonthSchedule(weekSchedules, monthDescription);
    }

    public TwoWeekSchedule prepareTwoWeekSchedule() {

        List<ScheduleTemplate> twoWeekTemplates =
                timetableService.getTwoWeekSchedule();
        Map<Period, List<ScheduleTemplate>> oddWeek = formatTemplatesIntoWeek(
                twoWeekTemplates, false);
        Map<Period, List<ScheduleTemplate>> evenWeek = formatTemplatesIntoWeek(
                twoWeekTemplates, true);

        return new TwoWeekSchedule(oddWeek, evenWeek);
    }

    private Map<Period, List<Schedule>> convertToPeriodDaySchedule(
            LocalDate date, List<Schedule> schedule) {

        return schedule.stream()
                .sorted()
                .filter(s -> s.getDate().equals(date))
                .collect(Collectors.groupingBy(Schedule::getPeriod,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
    }

    private Map<Period, List<ScheduleTemplate>> formatTemplatesIntoWeek(
            List<ScheduleTemplate> templates, boolean weekParity) {

        return templates.stream()
                .filter(t -> t.getWeekParity() == weekParity)
                .sorted()
                .collect(Collectors.groupingBy(ScheduleTemplate::getPeriod,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
    }

}
