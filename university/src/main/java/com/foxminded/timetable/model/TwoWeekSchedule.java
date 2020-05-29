package com.foxminded.timetable.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TwoWeekSchedule {

    private final Map<Period, List<ScheduleTemplate>> oddWeek;
    private final Map<Period, List<ScheduleTemplate>> evenWeek;

}
