package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
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
    private Long id;

    private String name;

    public Course(String name) {

        this.name = name;
    }

    @Override
    public int compareTo(Course other) {

        return this.name.compareTo(other.getName());
    }

}
