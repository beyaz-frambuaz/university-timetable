package com.foxminded.timetable.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.foxminded.timetable.service.SemesterCalendarUtils;

public class SemesterCalendarUtilsTest {

    private SemesterCalendarUtils semesterCalendar;

    @BeforeEach
    private void setUp() {
        this.semesterCalendar = new SemesterCalendarUtils("2020-09-07",
                "2020-12-11");
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
        boolean semesterDate = semesterCalendar
                .isSemesterDate(dateOutsideSemester);

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
    @CsvSource(value = { "10:2020-10-01", "12:2020-12-01" }, delimiter = ':')
    public void getMonthStartDateShouldReturnFirstOfMonthDateGivenMonthNumber(
            String month, String date) {

        int monthNumber = Integer.parseInt(month);
        LocalDate expected = LocalDate.parse(date);
        
        LocalDate actual = semesterCalendar.getMonthStartDate(monthNumber);

        assertThat(actual).isEqualTo(expected);
    }
    
    @ParameterizedTest
    @CsvSource(value = { "9:2020-09-30", "10:2020-10-31" }, delimiter = ':')
    public void getMonthEndDateShouldReturnLastOfMonthDateGivenMonthNumber(
            String month, String date) {
        
        int monthNumber = Integer.parseInt(month);
        LocalDate expected = LocalDate.parse(date);
        
        LocalDate actual = semesterCalendar.getMonthEndDate(monthNumber);
        
        assertThat(actual).isEqualTo(expected);
    }

}
