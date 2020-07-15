package com.foxminded.timetable.model;

import com.foxminded.timetable.constraints.IdValid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "st_seq")
    @SequenceGenerator(name = "st_seq", sequenceName = "student_id_seq")
    @IdValid("Student")
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @ManyToOne
    @JoinColumn
    @Valid
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
