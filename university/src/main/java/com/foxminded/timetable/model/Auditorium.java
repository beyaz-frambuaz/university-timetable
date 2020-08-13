package com.foxminded.timetable.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "auditoriums")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Auditorium implements Comparable<Auditorium> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "a_seq")
    @SequenceGenerator(name = "a_seq", sequenceName = "auditorium_id_seq")
    @Min(1)
    private Long id;

    @NotBlank
    private String name;

    public Auditorium(String name) {

        this.name = name;
    }

    @Override
    public int compareTo(Auditorium other) {

        return this.name.compareTo(other.getName());
    }

}
