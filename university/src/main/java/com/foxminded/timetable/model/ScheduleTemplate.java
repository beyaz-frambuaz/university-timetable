package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.DayOfWeek;

@Data
@AllArgsConstructor
public class ScheduleTemplate implements Comparable<ScheduleTemplate> {

    private Long       id;
    private boolean    weekParity;
    private DayOfWeek  day;
    private Period     period;
    private Auditorium auditorium;
    private Course     course;
    private Group      group;
    private Professor  professor;

    public ScheduleTemplate(boolean weekParity, DayOfWeek day, Period period,
            Auditorium auditorium, Course course, Group group,
            Professor professor) {

        this(null, weekParity, day, period, auditorium, course, group,
                professor);
    }

    public boolean getWeekParity() {

        return weekParity;
    }

    @Override
    public int compareTo(ScheduleTemplate other) {

        if (weekParity == other.getWeekParity()) {
            if (day == other.getDay()) {
                if (period == other.getPeriod()) {
                    if (auditorium.equals(other.getAuditorium())) {
                        if (group.equals(other.getGroup())) {
                            if (course.equals(other.getCourse())) {
                                return professor.compareTo(
                                        other.getProfessor());
                            }
                            return course.compareTo(other.getCourse());
                        }
                        return group.compareTo(other.getGroup());
                    }
                    return auditorium.compareTo(other.getAuditorium());
                }
                return period.compareTo(other.getPeriod());
            }
            return day.compareTo(other.getDay());
        }
        return Boolean.compare(weekParity, other.getWeekParity());
    }

}
