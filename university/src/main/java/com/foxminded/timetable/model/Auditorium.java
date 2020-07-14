package com.foxminded.timetable.model;

import com.foxminded.timetable.constraints.IdValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "auditoriums")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Auditorium implements Comparable<Auditorium> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "a_seq")
    @SequenceGenerator(name = "a_seq", sequenceName = "auditorium_id_seq")
    @IdValid("Auditorium")
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
