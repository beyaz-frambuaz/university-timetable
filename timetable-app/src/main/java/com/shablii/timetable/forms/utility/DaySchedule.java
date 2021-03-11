package com.shablii.timetable.forms.utility;

import com.shablii.timetable.model.*;
import lombok.Data;

import java.util.*;

@Data
public class DaySchedule {

    private final Map<Period, List<Schedule>> periodSchedules;
    private final String dayDescription;
    private final String[] dayShortDescription;

}
