package com.foxminded.university.timetable.model;

import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import lombok.Getter;

public class Timetable {
    private final SemesterProperties semesterProperties;
    @Getter private final Set<ReschedulingOption> reschedulingOptions;
    @Getter private List<ScheduleTemplate> scheduleTemplates;
    private List<Schedule> generatedSchedules;

    public Timetable(SemesterProperties semesterProperties,
            Set<ReschedulingOption> reschedulingOptions) {
        this.semesterProperties = semesterProperties;
        this.reschedulingOptions = reschedulingOptions;
        this.generatedSchedules = new ArrayList<>();
        this.scheduleTemplates = new ArrayList<>();
    }

    public List<Schedule> getPeriodSchedule(LocalDate date, Period period) {
        return isSemesterDate(date) ? lookupSchedule(date, period)
                : Collections.emptyList();
    }

    public List<Schedule> getDaySchedule(LocalDate date) {
        List<Schedule> daySchedules = new ArrayList<>();
        if (isSemesterDate(date)) {
            for (Period period : Period.values()) {
                daySchedules.addAll(lookupSchedule(date, period));
            }
            daySchedules.sort(Comparator.naturalOrder());
        }
        return daySchedules;
    }

    public List<Schedule> getRangeSchedule(LocalDate startDate,
            LocalDate endDate) {
        List<Schedule> schedules = new ArrayList<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {
            LocalDate date = startDate.plusDays(i);
            if (isSemesterDate(date)) {
                for (Period period : Period.values()) {
                    schedules.addAll(lookupSchedule(date, period));
                }
            }
        }
        schedules.sort(Comparator.naturalOrder());
        return schedules;
    }

    public void substituteProfessor(Schedule schedule, Professor professor) {
        schedule.setProfessor(professor);
    }

    private Map<LocalDate, List<ReschedulingOption>> getDayReschedulingOptions(
            Schedule candidate, LocalDate targetDate) {
        Map<LocalDate, List<ReschedulingOption>> result = new HashMap<>();

        if (!isSemesterDate(targetDate)) {
            return result;
        }

        List<ReschedulingOption> options = reschedulingOptions.stream()
                .filter(option -> option.getDay() == targetDate.getDayOfWeek())
                .filter(byTemplatesFreePeriodForAuditorium(targetDate))
                .filter(bySchedulesFreePeriodForAuditorium(targetDate))
                .filter(byTemplatesFreePeriodForProfessorGroup(candidate,
                        targetDate))
                .filter(bySchedulesFreePeriodForProfessorGroup(candidate,
                        targetDate))
                .sorted().collect(toList());

        result.put(targetDate, options);
        return result;
    }

    private Predicate<ReschedulingOption> byTemplatesFreePeriodForAuditorium(
            LocalDate targetDate) {
        return option -> scheduleTemplates.stream()
                .filter(template -> getWeekParityOf(targetDate) == template
                        .getWeekParity()
                        && template.getDay() == option.getDay())
                .noneMatch(
                        template -> template.getPeriod() == option.getPeriod()
                                && template.getAuditorium()
                                        .equals(option.getAuditorium()));
    }

    private Predicate<ReschedulingOption> bySchedulesFreePeriodForAuditorium(
            LocalDate targetDate) {
        return option -> generatedSchedules.stream()
                .filter(existingSchedule -> existingSchedule.getDate()
                        .equals(targetDate))
                .noneMatch(existingSchedule -> existingSchedule
                        .getPeriod() == option.getPeriod()
                        && existingSchedule.getAuditorium()
                                .equals(option.getAuditorium()));
    }

    private Predicate<ReschedulingOption> byTemplatesFreePeriodForProfessorGroup(
            Schedule candidate, LocalDate targetDate) {
        return option -> scheduleTemplates.stream()
                .filter(template -> getWeekParityOf(targetDate) == template
                        .getWeekParity()
                        && template.getDay() == option.getDay()
                        && (template.getGroup().equals(candidate.getGroup())
                                || template.getProfessor()
                                        .equals(candidate.getProfessor())))
                .noneMatch(
                        template -> template.getPeriod() == option.getPeriod());
    }

    private Predicate<ReschedulingOption> bySchedulesFreePeriodForProfessorGroup(
            Schedule candidate, LocalDate targetDate) {
        return option -> generatedSchedules.stream()
                .filter(existingSchedule -> existingSchedule.getDate()
                        .equals(targetDate)
                        && (existingSchedule.getGroup()
                                .equals(candidate.getGroup())
                                || existingSchedule.getProfessor()
                                        .equals(candidate.getProfessor())))
                .noneMatch(
                        template -> template.getPeriod() == option.getPeriod());
    }

    public Map<LocalDate, List<ReschedulingOption>> getReschedulingOptions(
            Schedule candidate, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<ReschedulingOption>> results = new HashMap<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {
            LocalDate date = startDate.plusDays(i);
            results.putAll(getDayReschedulingOptions(candidate, date));
        }
        return results;
    }

    public void rescheduleOnce(Schedule candidate, LocalDate date,
            ReschedulingOption targetOption) {
        candidate.setDate(date);
        candidate.setDay(targetOption.getDay());
        candidate.setPeriod(targetOption.getPeriod());
        candidate.setAuditorium(targetOption.getAuditorium());
    }

    public void reschedulePermanently(Schedule candidate, LocalDate date,
            ReschedulingOption targetOption) {
        ScheduleTemplate template = candidate.getScheduleTemplate();
        template.setWeekParity(getWeekParityOf(date));
        template.setDay(targetOption.getDay());
        template.setPeriod(targetOption.getPeriod());
        template.setAuditorium(targetOption.getAuditorium());
        template.setCourse(candidate.getCourse());
        template.setGroup(candidate.getGroup());
        template.setProfessor(candidate.getProfessor());
        updateSchedules(template, date);
    }

    private void updateSchedules(ScheduleTemplate template, LocalDate date) {
        generatedSchedules.stream().filter(
                schedule -> schedule.getScheduleTemplate().equals(template))
                .forEach(schedule -> schedule.update(date));
    }

    private List<Schedule> lookupSchedule(LocalDate date, Period period) {
        return scheduleTemplates.stream().filter(
                template -> getWeekParityOf(date) == template.getWeekParity()
                        && template.getDay() == date.getDayOfWeek()
                        && template.getPeriod() == period)
                .map(template -> generatedSchedules.stream()
                        .filter(schedule -> schedule.getScheduleTemplate()
                                .equals(template)
                                && schedule.getDate().isEqual(date))
                        .findFirst().orElseGet(
                                () -> generateAndSaveSchedule(template, date)))
                .sorted().collect(toList());
    }

    private Schedule generateAndSaveSchedule(ScheduleTemplate template,
            LocalDate date) {
        Schedule newSchedule = new Schedule(template, date);
        generatedSchedules.add(newSchedule);
        return newSchedule;
    }

    private boolean getWeekParityOf(LocalDate date) {
        int dateWeekInYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekInYearBeforeSemester = semesterProperties.getStartDate()
                .minusDays(1).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return (dateWeekInYear - weekInYearBeforeSemester) % 2 == 0;
    }

    private boolean isSemesterDate(LocalDate date) {
        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.isBefore(semesterProperties.getStartDate())
                || date.isAfter(semesterProperties.getEndDate()));
    }

    public void addScheduleTemplate(ScheduleTemplate scheduleTemplate) {
        scheduleTemplates.add(scheduleTemplate);
    }

    public LocalDate getWeekMonday(int weekNumber) {
        return semesterProperties.getStartDate().plusWeeks(weekNumber - 1L);
    }

    public LocalDate getWeekFriday(int weekNumber) {
        return semesterProperties.getEndDate()
                .minusWeeks(semesterProperties.getLengthInWeeks() - weekNumber);
    }

    public LocalDate getMonthStartDate(int month) {
        return LocalDate.of(semesterProperties.getStartDate().getYear(), month,
                1);
    }

    public LocalDate getMonthEndDate(int month) {
        return LocalDate.of(semesterProperties.getStartDate().getYear(), month,
                Month.of(month).length(
                        semesterProperties.getStartDate().isLeapYear()));
    }
}
