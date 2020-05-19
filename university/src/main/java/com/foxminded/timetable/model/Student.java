package com.foxminded.timetable.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class Student {

    private final String firstName;
    private final String lastName;
    private       Long   id;
    private       Group  group;

    public Student(Long id, String firstName, String lastName, Group group) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.id = id;
        this.group = group;
    }

    public String getFullName() {

        return firstName + " " + lastName;
    }

}
