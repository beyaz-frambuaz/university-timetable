package com.foxminded.timetable.web.utils;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Schedule;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WeekScheduleView {

    private final Map<Period, List<Schedule>> schedules;
    private final boolean filtered;
    private final String weekDescription;
    private final int weekNumber;
    private final String mon;
    private final String tue;
    private final String wed;
    private final String thu;
    private final String fri;

}
