package com.foxminded.timetable.web.utils;

import lombok.Data;

import java.util.List;

@Data
public class MonthScheduleView {

    private final List<WeekScheduleView> weekSchedules;
    private final boolean filtered;
    private final String monthDescription;

}
