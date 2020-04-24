package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Group implements Comparable<Group> {
    private long id;
    private final String name;

    @Override
    public int compareTo(Group other) {
        return name.compareTo(other.getName());
    }
}
