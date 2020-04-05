package com.foxminded.university.timetable.model;

import lombok.Data;

@Data
public class Course implements Comparable<Course>{
    private final String name;

    @Override
    public int compareTo(Course other) {
        return this.name.compareTo(other.getName());
    }
}
