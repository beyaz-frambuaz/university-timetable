package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Group implements Comparable<Group> {

    private Long   id;
    private String name;

    public Group(String name) {

        this.name = name;
    }

    @Override
    public int compareTo(Group other) {

        return name.compareTo(other.getName());
    }

}
