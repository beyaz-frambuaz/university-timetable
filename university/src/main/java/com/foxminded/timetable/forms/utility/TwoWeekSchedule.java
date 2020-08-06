package com.foxminded.timetable.forms.utility;

import com.foxminded.timetable.model.*;
import lombok.Data;

import java.util.*;

@Data
public class TwoWeekSchedule {

    private final Map<Period, List<ScheduleTemplate>> oddWeek;
    private final Map<Period, List<ScheduleTemplate>> evenWeek;

}
