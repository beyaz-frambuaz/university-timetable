package com.foxminded.timetable.forms.utility;

import lombok.Data;

import java.util.List;

@Data
public class MonthSchedule {

    private final List<WeekSchedule> weekSchedules;
    private final String             monthDescription;

}
