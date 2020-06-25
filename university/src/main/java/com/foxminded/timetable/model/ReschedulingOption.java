package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
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
    @SequenceGenerator(name = "r_o_seq",
                       sequenceName = "rescheduling_option_id_seq")
    private long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeek day;

    @Enumerated(EnumType.STRING)
    private Period period;

    @ManyToOne
    @JoinColumn
    private Auditorium auditorium;

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
