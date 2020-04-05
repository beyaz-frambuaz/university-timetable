package com.foxminded.university.timetable.model;

import lombok.Data;

@Data
public class Auditorium implements Comparable<Auditorium>{
    private final String name;
    
    @Override
    public int compareTo(Auditorium other) {
        return this.name.compareTo(other.getName());
    }
}
