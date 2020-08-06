package com.foxminded.timetable.service.utility;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.*;
import java.time.temporal.*;
import java.util.Locale;

@Getter
@Component
public class SemesterCalendar {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Integer lengthInWeeks;
    private Semester semester;

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
        int weekInYearBeforeSemester =
                startDate.minusDays(1).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return (dateWeekInYear - weekInYearBeforeSemester) % 2 == 0;
    }

    public boolean isSemesterDate(LocalDate date) {

        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY || date.isBefore(
                startDate) || date.isAfter(endDate));
    }

    public boolean isSemesterWeek(int week) {

        return week > 0 && week <= lengthInWeeks;
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
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.US)) + " - " + endDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.US));
    }

    public String getDayDescription(LocalDate date) {

        return date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(Locale.US));
    }

    public String getWeekDescription(LocalDate date) {

        LocalDate monday = getWeekMonday(date);
        LocalDate friday = getWeekFriday(date);
        return monday.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.US)) + " - " + friday.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.US));
    }

    public String getMonthDescription(LocalDate date) {

        LocalDate firstOfMonth = getFirstSemesterDayOfMonth(date);
        LocalDate lastOfMonth = getLastSemesterDayOfMonth(date);
        return firstOfMonth.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.US)) + " - " + lastOfMonth.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.US));
    }

    public int getSemesterWeekNumber(LocalDate date) {

        int dateWeekInYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekInYearBeforeSemester =
                startDate.minusDays(1).get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return dateWeekInYear - weekInYearBeforeSemester;
    }

    public String[] getDayShortDescription(LocalDate date) {

        return new String[] { date.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.US).toUpperCase(),
                date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                        .withLocale(Locale.US)) };
    }

    public Semester getSemester() {

        if (semester == null) {
            semester = new Semester(startDate, endDate, lengthInWeeks);
        }
        return semester;
    }

    @Getter
    @RequiredArgsConstructor
    public static class Semester {

        private final LocalDate startDate;
        private final LocalDate endDate;
        private final Integer lengthInWeeks;

    }

}
