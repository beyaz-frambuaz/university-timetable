package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Auditorium implements Comparable<Auditorium> {
    private long id;
    private final String name;
    
    @Override
    public int compareTo(Auditorium other) {
        return this.name.compareTo(other.getName());
    }
}
