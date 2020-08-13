package com.foxminded.timetable.model;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.*;

@Entity
@Table(name = "schedules")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Schedule implements Comparable<Schedule> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sch_seq")
    @SequenceGenerator(name = "sch_seq", sequenceName = "schedule_id_seq")
    @Min(1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private ScheduleTemplate template;

    @Column(name = "on_date")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate date;

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

    public Schedule(ScheduleTemplate template, LocalDate date) {

        this(null, template, date, template.getDay(), template.getPeriod(),
                template.getAuditorium(), template.getCourse(),
                template.getGroup(), template.getProfessor());
    }

    public Schedule(Schedule other) {

        this(other.id, other.template, other.date, other.day, other.period,
                other.auditorium, other.course, other.group, other.professor);
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
