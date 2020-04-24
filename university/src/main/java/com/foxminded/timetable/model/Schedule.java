package com.foxminded.timetable.model;

import java.time.DayOfWeek;
import java.time.LocalDate;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Schedule implements Comparable<Schedule> {

    private final long id;
    private final long templateId;
    private final LocalDate date;
    private final DayOfWeek day;
    private final Period period;
    private final Auditorium auditorium;
    private final Course course;
    private final Group group;
    private final Professor professor;
    private ScheduleTemplate scheduleTemplate;
    
    public Schedule(ScheduleTemplate scheduleTemplate, LocalDate date) {
        
        this.id = 0;
        this.templateId = scheduleTemplate.getId();
        this.scheduleTemplate = scheduleTemplate;
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
