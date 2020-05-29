package com.foxminded.timetable.model;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DayOptions {

    private final Map<Period, List<ReschedulingOption>> periodOptions;
    private final String                                dayDescription;
    private final String[]                              dayShortDescription;
    private final String                                date;

}
