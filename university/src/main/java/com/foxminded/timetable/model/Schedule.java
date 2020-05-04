package com.foxminded.timetable.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Schedule implements Comparable<Schedule> {

    private final Long id;
    private final Long templateId;
    private LocalDate date;
    private DayOfWeek day;
    private Period period;
    private Auditorium auditorium;
    private Course course;
    private Group group;
    private Professor professor;

    public Schedule(ScheduleTemplate template, LocalDate date) {

        this(null, template.getId(), date, template.getDay(),
                template.getPeriod(), template.getAuditorium(),
                template.getCourse(), template.getGroup(),
                template.getProfessor());
    }

    @Override
    public int compareTo(Schedule other) {
        if (date.compareTo(other.getDate()) == 0) {
            if (period.compareTo(other.getPeriod()) == 0) {
                if (auditorium.equals(other.getAuditorium())) {
                    if (group.equals(other.getGroup())) {
                        if (course.equals(other.getCourse())) {
                            return professor.compareTo(other.getProfessor());
                        }
                        return course.compareTo(other.getCourse());
                    }
                    return group.compareTo(other.getGroup());
                }
                return auditorium.compareTo(other.getAuditorium());
            }
            return period.compareTo(other.getPeriod());
        }
        return date.compareTo(other.getDate());
    }
}
