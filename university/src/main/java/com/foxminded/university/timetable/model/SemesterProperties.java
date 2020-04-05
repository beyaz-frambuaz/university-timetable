package com.foxminded.university.timetable.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import lombok.Getter;

public class SemesterProperties {
    @Getter private final LocalDate startDate;
    @Getter private final LocalDate endDate;
    @Getter private final Integer lengthInWeeks;

    public SemesterProperties(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.lengthInWeeks = (int) ChronoUnit.WEEKS.between(startDate, endDate)
                + 1;
    }
}
