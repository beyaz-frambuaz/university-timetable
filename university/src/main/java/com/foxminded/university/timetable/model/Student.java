package com.foxminded.university.timetable.model;

import lombok.Data;

@Data
public class Student {
    private final String firstName;
    private final String lastName;
    private long studentId;
    private Group group;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
