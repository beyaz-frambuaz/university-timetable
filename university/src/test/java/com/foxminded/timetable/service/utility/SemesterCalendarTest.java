package com.foxminded.timetable.service.utility;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class SemesterCalendarTest {

    private SemesterCalendar semesterCalendar;

    @BeforeEach
    private void setUp() {

        this.semesterCalendar =
                new SemesterCalendar("2020-09-07", "2020-12-11");
    }

    @ParameterizedTest
    @ValueSource(strings = { "2020-09-07", "2020-11-07", "2020-12-01" })
    public void getWeekParityShouldReturnFalseGivenDatesWithinOddWeekNumbers(
            String date) {

        LocalDate oddWeekDate = LocalDate.parse(date);
        boolean weekParity = semesterCalendar.getWeekParityOf(oddWeekDate);

        assertThat(weekParity).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2020-09-14", "2020-11-14", "2020-12-08" })
    public void getWeekParityShouldReturnTrueGivenDatesWithinEvenWeekNumbers(
            String date) {

        LocalDate evenWeekDate = LocalDate.parse(date);
        boolean weekParity = semesterCalendar.getWeekParityOf(evenWeekDate);

        assertThat(weekParity).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2020-01-01", "2020-09-06", "2020-12-14" })
    public void isSemesterDateShouldReturnFalseGivenDateOutsideSemester(
            String date) {

        LocalDate dateOutsideSemester = LocalDate.parse(date);
        boolean semesterDate =
                semesterCalendar.isSemesterDate(dateOutsideSemester);

        assertThat(semesterDate).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2020-09-12", "2020-11-14", "2020-12-06" })
    public void isSemesterDateShouldReturnFalseGivenWeekendDateWithinSemester(
            String date) {

        LocalDate weekend = LocalDate.parse(date);
        boolean semesterDate = semesterCalendar.isSemesterDate(weekend);

        assertThat(semesterDate).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = { "2020-10-13", "2020-11-06", "2020-12-09" })
    public void isSemesterDateShouldReturnTrueGivenWorkDateWithinSemester(
            String date) {

        LocalDate workDay = LocalDate.parse(date);
        boolean semesterDate = semesterCalendar.isSemesterDate(workDay);

        assertThat(semesterDate).isTrue();
    }

    @ParameterizedTest
    @CsvSource(value = { "1:2020-09-07", "8:2020-10-26" }, delimiter = ':')
    public void getWeekMondayShouldReturnMondayDateGivenSemesterWeek(
            String week, String date) {

        int weekNumber = Integer.parseInt(week);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual = semesterCalendar.getWeekMonday(weekNumber);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-10:2020-09-07", "2020-11-01:2020-10-26" },
               delimiter = ':')
    public void getWeekMondayShouldReturnMondayDateGivenAnyWeekDate(
            String anyWeekDate, String date) {

        LocalDate anyDateInWeek = LocalDate.parse(anyWeekDate);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual = semesterCalendar.getWeekMonday(anyDateInWeek);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
    }

    @ParameterizedTest
    @CsvSource(value = { "3:2020-09-25", "12:2020-11-27" }, delimiter = ':')
    public void getWeekFridayShouldReturnFridayDateGivenSemesterWeek(
            String week, String date) {

        int weekNumber = Integer.parseInt(week);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual = semesterCalendar.getWeekFriday(weekNumber);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-21:2020-09-25", "2020-11-28:2020-11-27" },
               delimiter = ':')
    public void getWeekFridayShouldReturnFridayDateGivenAnyWeekDate(
            String anyWeekDate, String date) {

        LocalDate anyDateInWeek = LocalDate.parse(anyWeekDate);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual = semesterCalendar.getWeekFriday(anyDateInWeek);

        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
    }

    @ParameterizedTest
    @CsvSource(value = { "9:2020-09-07", "11:2020-11-02", "12:2020-12-01" },
               delimiter = ':')
    public void getFirstSemesterDayOfMonthShouldReturnFirstSemesterDateInMonthGivenMonthNumber(
            String month, String date) {

        int monthNumber = Integer.parseInt(month);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual =
                semesterCalendar.getFirstSemesterDayOfMonth(monthNumber);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-01:2020-09-07", "2020-11-20:2020-11-02",
            "2020-12-31:2020-12-01" }, delimiter = ':')
    public void getFirstSemesterDayOfMonthShouldReturnFirstSemesterDateInMonthGivenAnyMonthDate(
            String anyMonthDate, String date) {

        LocalDate anyDateInMonth = LocalDate.parse(anyMonthDate);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual =
                semesterCalendar.getFirstSemesterDayOfMonth(anyDateInMonth);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "9:2020-09-30", "10:2020-10-30", "12:2020-12-11" },
               delimiter = ':')
    public void getLastSemesterDayOfMonthShouldReturnLastSemesterDateInMonthGivenMonthNumber(
            String month, String date) {

        int monthNumber = Integer.parseInt(month);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual =
                semesterCalendar.getLastSemesterDayOfMonth(monthNumber);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-01:2020-09-30", "2020-10-31:2020-10-30",
            "2020-12-31:2020-12-11" }, delimiter = ':')
    public void getLastSemesterDayOfMonthShouldReturnLastSemesterDateInMonthGivenAnyMonthDate(
            String anyMonthDate, String date) {

        LocalDate anyDateInMonth = LocalDate.parse(anyMonthDate);
        LocalDate expected = LocalDate.parse(date);

        LocalDate actual =
                semesterCalendar.getLastSemesterDayOfMonth(anyDateInMonth);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getSemesterDescriptionShouldReturnFormattedString() {

        String expected = "Sep 7, 2020 - Dec 11, 2020";

        String actual = semesterCalendar.getSemesterDescription();

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-07:Sep 7, 2020", "2020-12-02:Dec 2, 2020" },
               delimiter = ':')
    public void getDayDescriptionShouldReturnFormattedDateDescription(
            String anyDate, String expected) {

        LocalDate date = LocalDate.parse(anyDate);

        String actual = semesterCalendar.getDayDescription(date);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-07:Sep 7, 2020 - Sep 11, 2020",
            "2020-12-02:Nov 30, 2020 - Dec 4, 2020" }, delimiter = ':')
    public void getWeekDescriptionShouldReturnSemesterWeekDescriptionGivenAnyWeekDate(
            String anyWeekDate, String expected) {

        LocalDate anyDateInWeek = LocalDate.parse(anyWeekDate);

        String actual = semesterCalendar.getWeekDescription(anyDateInWeek);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-07:Sep 7, 2020 - Sep 30, 2020",
            "2020-12-02:Dec 1, 2020 - Dec 11, 2020" }, delimiter = ':')
    public void getMonthDescriptionShouldReturnSemesterMonthDescriptionGivenAnyMonthDate(
            String anyMonthDate, String expected) {

        LocalDate anyDateInMonth = LocalDate.parse(anyMonthDate);

        String actual = semesterCalendar.getMonthDescription(anyDateInMonth);

        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = { "2020-09-07:1", "2020-11-15:10", "2020-12-02:13" },
               delimiter = ':')
    public void getSemesterWeekNumberShouldReturnSemesterWeekNumberGivenAnyWeekDate(
            String anyWeekDate, String weekNumber) {

        LocalDate anyDateInWeek = LocalDate.parse(anyWeekDate);
        int expected = Integer.parseInt(weekNumber);

        int actual = semesterCalendar.getSemesterWeekNumber(anyDateInWeek);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getDayShortDescriptionShouldReturnFormattedDateDescription() {

        LocalDate date = LocalDate.of(2020, 1, 1);
        String[] expected = {"WED", "1/1/20"};

        String[] actual = semesterCalendar.getDayShortDescription(date);

        assertThat(actual).isEqualTo(expected);
    }

}
