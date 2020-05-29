package com.foxminded.timetable.service;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;
import java.util.Locale;

@Getter
@Component
public class SemesterCalendar {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer   lengthInWeeks;

    public SemesterCalendar(
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

    public LocalDate getFirstSemesterDayOfMonth(int month) {

        LocalDate firstOfMonth = LocalDate.of(startDate.getYear(), month, 1);
        while (!isSemesterDate(firstOfMonth)) {
            firstOfMonth = firstOfMonth.plusDays(1L);
        }

        return firstOfMonth;
    }

    public LocalDate getFirstSemesterDayOfMonth(LocalDate anyMonthDate) {

        return getFirstSemesterDayOfMonth(anyMonthDate.getMonthValue());
    }

    public LocalDate getLastSemesterDayOfMonth(int month) {

        LocalDate lastOfMonth = LocalDate.of(startDate.getYear(), month,
                Month.of(month).length(startDate.isLeapYear()));
        while (!isSemesterDate(lastOfMonth)) {
            lastOfMonth = lastOfMonth.minusDays(1L);
        }

        return lastOfMonth;
    }

    public LocalDate getLastSemesterDayOfMonth(LocalDate anyMonthDate) {

        return getLastSemesterDayOfMonth(anyMonthDate.getMonthValue());
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

        LocalDate firstOfMonth = getFirstSemesterDayOfMonth(date);
        LocalDate lastOfMonth = getLastSemesterDayOfMonth(date);
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

    public String getDayDescription(LocalDate date) {

        return date.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public String[] getDayShortDescription(LocalDate date) {

        return new String[] { date.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase(),
                date.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)) };
    }

}
