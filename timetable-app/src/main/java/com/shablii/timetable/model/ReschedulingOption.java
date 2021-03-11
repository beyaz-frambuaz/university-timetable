package com.shablii.timetable.model;

import lombok.*;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.DayOfWeek;

@Entity
@Table(name = "rescheduling_options")
@Immutable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReschedulingOption implements Comparable<ReschedulingOption> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "r_o_seq")
    @SequenceGenerator(name = "r_o_seq", sequenceName = "rescheduling_option_id_seq")
    @Min(1)
    private Long id;

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

    public ReschedulingOption(DayOfWeek day, Period period, Auditorium auditorium) {

        this(null, day, period, auditorium);
    }

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
