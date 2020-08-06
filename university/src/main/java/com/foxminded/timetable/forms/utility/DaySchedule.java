package com.foxminded.timetable.forms.utility;

import com.foxminded.timetable.model.*;
import lombok.Data;

import java.util.*;

@Data
public class DaySchedule {

    private final Map<Period, List<Schedule>> periodSchedules;
    private final String dayDescription;
    private final String[] dayShortDescription;

}
