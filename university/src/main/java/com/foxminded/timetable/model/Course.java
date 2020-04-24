package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Course implements Comparable<Course>{
    private long id;
    private final String name;

    @Override
    public int compareTo(Course other) {
        return this.name.compareTo(other.getName());
    }
}
