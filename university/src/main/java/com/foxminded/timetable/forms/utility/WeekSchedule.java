package com.foxminded.timetable.forms.utility;

import lombok.Data;

import java.util.List;

@Data
public class WeekSchedule {

    private final List<DaySchedule> daySchedules;
    private final String weekDescription;
    private final int weekNumber;

}
