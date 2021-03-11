package com.shablii.timetable.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course implements Comparable<Course>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "c_seq")
    @SequenceGenerator(name = "c_seq", sequenceName = "course_id_seq")
    @Min(1)
    private Long id;

    @NotBlank
    private String name;

    public Course(String name) {

        this.name = name;
    }

    @Override
    public int compareTo(Course other) {

        return this.name.compareTo(other.getName());
    }

}
