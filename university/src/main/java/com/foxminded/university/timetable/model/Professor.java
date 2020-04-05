package com.foxminded.university.timetable.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class Professor implements Comparable<Professor> {
    private final String firstName;
    private final String lastName;
    private List<Course> courses;
    
    public Professor(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.courses = new ArrayList<>();
    }

    public void addCourse(Course course) {
        courses.add(course);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public int compareTo(Professor other) {
        if (firstName.equals(other.getFirstName())) {
            if (lastName.equals(other.getLastName())) {
                return courses.equals(other.getCourses()) ? 0 : 1;
            }
            return lastName.compareTo(other.getLastName());
        }
        return firstName.compareTo(other.getFirstName());
    }
}
