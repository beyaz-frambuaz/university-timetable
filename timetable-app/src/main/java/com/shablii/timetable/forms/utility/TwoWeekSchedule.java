package com.shablii.timetable.forms.utility;

import com.shablii.timetable.model.*;
import lombok.Data;

import java.util.*;

@Data
public class TwoWeekSchedule {

    private final Map<Period, List<ScheduleTemplate>> oddWeek;
    private final Map<Period, List<ScheduleTemplate>> evenWeek;

}
