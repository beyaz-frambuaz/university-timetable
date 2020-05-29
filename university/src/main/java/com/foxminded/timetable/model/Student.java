package com.foxminded.timetable.model;

import lombok.Data;

@Data
public class Student {

    private String firstName;
    private String lastName;
    private Long   id;
    private Group  group;

    public Student(String firstName, String lastName) {

        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Student(String firstName, String lastName, Group group) {

        this(firstName, lastName);
        this.group = group;
    }

    public Student(Long id, String firstName, String lastName, Group group) {

        this(firstName, lastName, group);
        this.id = id;
    }

    public String getFullName() {

        return firstName + " " + lastName;
    }

}
