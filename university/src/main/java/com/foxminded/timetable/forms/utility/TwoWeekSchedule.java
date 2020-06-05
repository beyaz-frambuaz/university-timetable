package com.foxminded.timetable.forms.utility;

import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.ScheduleTemplate;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TwoWeekSchedule {

    private final Map<Period, List<ScheduleTemplate>> oddWeek;
    private final Map<Period, List<ScheduleTemplate>> evenWeek;

}
