package com.foxminded.timetable.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.time.temporal.IsoFields;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
public class SemesterCalendarUtils {
    @Getter
    private final LocalDate startDate;
    @Getter
    private final LocalDate endDate;
    @Getter
    private final Integer lengthInWeeks;

    public SemesterCalendarUtils(
            @Value("${university.semester.start.date}") String startDate,
            @Value("${university.semester.end.date}") String endDate) {
        this.startDate = LocalDate.parse(startDate);
        this.endDate = LocalDate.parse(endDate);
        this.lengthInWeeks = (int) ChronoUnit.WEEKS.between(this.startDate,
                this.endDate) + 1;
    }

    public boolean getWeekParityOf(LocalDate date) {
        int dateWeekInYear = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int weekInYearBeforeSemester = startDate.minusDays(1)
                .get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        return (dateWeekInYear - weekInYearBeforeSemester) % 2 == 0;
    }

    public boolean isSemesterDate(LocalDate date) {
        return !(date.getDayOfWeek() == DayOfWeek.SATURDAY
                || date.getDayOfWeek() == DayOfWeek.SUNDAY
                || date.isBefore(startDate) || date.isAfter(endDate));
    }

    public LocalDate getWeekMonday(int weekNumber) {
        return startDate.plusWeeks(weekNumber - 1L);
    }

    public LocalDate getWeekFriday(int weekNumber) {
        return endDate.minusWeeks((long) (lengthInWeeks - weekNumber));
    }

    public LocalDate getMonthStartDate(int month) {
        return LocalDate.of(startDate.getYear(), month, 1);
    }

    public LocalDate getMonthEndDate(int month) {
        return LocalDate.of(startDate.getYear(), month,
                Month.of(month).length(startDate.isLeapYear()));
    }
}
