package com.foxminded.timetable.forms.utility;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Schedule;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DaySchedule {

    private final Map<Period, List<Schedule>> periodSchedules;
    private final String                      dayDescription;
    private final String[]                    dayShortDescription;

}
