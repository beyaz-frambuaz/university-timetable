package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "st_seq")
    @SequenceGenerator(name = "st_seq", sequenceName = "student_id_seq")
    private Long id;

    private String firstName;

    private String lastName;

    @ManyToOne
    @JoinColumn
    private Group group;

    public Student(String firstName, String lastName) {

        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Student(String firstName, String lastName, Group group) {

        this(firstName, lastName);
        this.group = group;
    }

    public String getFullName() {

        return firstName + " " + lastName;
    }

}
