package com.foxminded.timetable.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.*;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "st_seq")
    @SequenceGenerator(name = "st_seq", sequenceName = "student_id_seq")
    @Min(1)
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

    @JsonIgnore
    public String getFullName() {

        return firstName + " " + lastName;
    }

}
