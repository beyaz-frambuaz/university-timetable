package com.foxminded.university.timetable.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import lombok.Data;

@Data
public class Schedule implements Comparable<Schedule>{
    private final ScheduleTemplate scheduleTemplate;
    private LocalDate date;
    private DayOfWeek day;
    private Period period;
    private Auditorium auditorium;
    private Course course;
    private Group group;
    private Professor professor;

    public Schedule(ScheduleTemplate scheduleTemplate, LocalDate date) {
        this.scheduleTemplate = scheduleTemplate;
        update(date);
    }

    public void update(LocalDate date) {
        this.date = date;
        this.day = scheduleTemplate.getDay();
        this.period = scheduleTemplate.getPeriod();
        this.auditorium = scheduleTemplate.getAuditorium();
        this.course = scheduleTemplate.getCourse();
        this.group = scheduleTemplate.getGroup();
        this.professor = scheduleTemplate.getProfessor();
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
