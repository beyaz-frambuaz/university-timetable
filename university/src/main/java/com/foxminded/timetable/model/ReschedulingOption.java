package com.foxminded.timetable.model;

import lombok.Data;

import java.time.DayOfWeek;

@Data
public class ReschedulingOption implements Comparable<ReschedulingOption> {

    private final long       id;
    private final DayOfWeek  day;
    private final Period     period;
    private final Auditorium auditorium;

    @Override
    public int compareTo(ReschedulingOption other) {

        if (day == other.getDay()) {
            if (period == other.getPeriod()) {
                return auditorium.compareTo(other.getAuditorium());
            }
            return period.compareTo(other.getPeriod());
        }
        return day.compareTo(other.getDay());
    }

}
