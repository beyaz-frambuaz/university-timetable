package com.shablii.timetable.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.DayOfWeek;

@Entity
@Table(name = "schedule_templates")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleTemplate implements Comparable<ScheduleTemplate> {

    @Id
    @Access(AccessType.PROPERTY)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "s_t_seq")
    @SequenceGenerator(name = "s_t_seq", sequenceName = "schedule_template_id_seq")
    @Min(1)
    private Long id;

    private boolean weekParity;

    @Enumerated(EnumType.STRING)
    @NotNull
    private DayOfWeek day;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Period period;

    @ManyToOne
    @JoinColumn
    @Valid
    @NotNull
    private Auditorium auditorium;

    @ManyToOne
    @JoinColumn
    @Valid
    @NotNull
    private Course course;

    @ManyToOne
    @JoinColumn
    @Valid
    @NotNull
    private Group group;

    @ManyToOne
    @JoinColumn
    @Valid
    @NotNull
    private Professor professor;

    public ScheduleTemplate(boolean weekParity, DayOfWeek day, Period period, Auditorium auditorium, Course course,
            Group group, Professor professor) {

        this(null, weekParity, day, period, auditorium, course, group, professor);
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
            return day.compareTo(other.getDay());
        }
        return Boolean.compare(weekParity, other.getWeekParity());
    }

}
