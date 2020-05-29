package com.foxminded.timetable.model;

import lombok.Data;

import java.util.List;

@Data
public class WeekSchedule {

    private final List<DaySchedule> daySchedules;
    private final String            weekDescription;
    private final int               weekNumber;

}
