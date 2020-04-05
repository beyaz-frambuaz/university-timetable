package com.foxminded.university.timetable.model;

import java.util.List;

import lombok.Data;

@Data
public class Group implements Comparable<Group> {
    private final String name;
    private final List<Course> courses;

    @Override
    public int compareTo(Group other) {
        return name.compareTo(other.getName());
    }
}
