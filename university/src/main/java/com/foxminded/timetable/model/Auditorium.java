package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Auditorium implements Comparable<Auditorium> {

    private Long id;
    private String name;

    public Auditorium(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Auditorium other) {
        return this.name.compareTo(other.getName());
    }
}
