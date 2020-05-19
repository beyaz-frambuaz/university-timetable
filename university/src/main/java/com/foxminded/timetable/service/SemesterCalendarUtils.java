package com.foxminded.timetable.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;

@Component
public class SemesterCalendarUtils {

    @Getter
    private final LocalDate startDate;
    @Getter
    private final LocalDate endDate;
    @Getter
    private final Integer   lengthInWeeks;

    public SemesterCalendarUtils(
            @Value("${university.semester.start.date}") String startDate,
            @Value("${university.semester.end.date}") String endDate) {

        this.startDate = LocalDate.parse(startDate);
        this.endDate = LocalDate.parse(endDate);
        this.lengthInWeeks =
                (int) ChronoUnit.WEEKS.between(this.startDate, this.endDate)
                        + 1;
    }

    public boolean getWeekParityOf(LocalDate date) {

        int dateWeekInYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekInYearBeforeSemester = startDate.minusDays(1)
                .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return (dateWeekInYear - weekInYearBeforeSemester) % 2 == 0;
    }

    public boolean isSemesterDate(LocalDate date) {

        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY || date.isBefore(
                startDate) || date.isAfter(endDate));
    }

    public LocalDate getWeekMonday(int weekNumber) {

        return startDate.plusWeeks(weekNumber - 1L);
    }

    public LocalDate getWeekMonday(LocalDate anyWeekDate) {

        int weekNumber = getSemesterWeekNumber(anyWeekDate);
        return startDate.plusWeeks(weekNumber - 1L);
    }

    public LocalDate getWeekFriday(int weekNumber) {

        return endDate.minusWeeks(lengthInWeeks - weekNumber);
    }

    public LocalDate getWeekFriday(LocalDate anyWeekDate) {

        int weekNumber = getSemesterWeekNumber(anyWeekDate);
        return endDate.minusWeeks(lengthInWeeks - weekNumber);
    }

    public LocalDate getFirstSemesterMondayOfMonth(int month) {

        LocalDate firstMonday = LocalDate.of(startDate.getYear(), month, 1);
        while (!isSemesterDate(firstMonday)) {
            firstMonday = firstMonday.plusDays(1L);
        }

        return firstMonday;
    }

    public LocalDate getFirstSemesterMondayOfMonth(LocalDate anyMonthDate) {

        return getFirstSemesterMondayOfMonth(anyMonthDate.getMonthValue());
    }

    public LocalDate getLastSemesterFridayOfMonth(int month) {

        LocalDate lastFriday = LocalDate.of(startDate.getYear(), month,
                Month.of(month).length(startDate.isLeapYear()));
        while (!isSemesterDate(lastFriday)) {
            lastFriday = lastFriday.minusDays(1L);
        }

        return lastFriday;
    }

    public LocalDate getLastSemesterFridayOfMonth(LocalDate anyMonthDate) {

        return getLastSemesterFridayOfMonth(anyMonthDate.getMonthValue());
    }

    public String getSemesterDescription() {

        return startDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) + " - "
                + endDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public String getWeekDescription(LocalDate date) {

        LocalDate monday = getWeekMonday(date);
        LocalDate friday = getWeekFriday(date);
        return monday.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) + " - "
                + friday.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public String getMonthDescription(LocalDate date) {

        LocalDate firstOfMonth = getFirstSemesterMondayOfMonth(date);
        LocalDate lastOfMonth = getLastSemesterFridayOfMonth(date);
        return firstOfMonth.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) + " - "
                + lastOfMonth.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public int getSemesterWeekNumber(LocalDate date) {

        int dateWeekInYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekInYearBeforeSemester = startDate.minusDays(1)
                .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return dateWeekInYear - weekInYearBeforeSemester;
    }

}
