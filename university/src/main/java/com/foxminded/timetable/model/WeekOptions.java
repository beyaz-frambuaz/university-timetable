package com.foxminded.timetable.model;

import lombok.Data;

import java.util.List;

@Data
public class WeekOptions {

    private final List<DayOptions> dayOptions;
    private final String           weekDescription;
    private final int              weekNumber;

}
