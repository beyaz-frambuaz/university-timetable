package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Student {

    private Long id;
    private final String firstName;
    private final String lastName;
    private Group group;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
