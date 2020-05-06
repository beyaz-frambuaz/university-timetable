package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Course implements Comparable<Course> {

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
