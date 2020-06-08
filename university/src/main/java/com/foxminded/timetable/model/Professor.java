package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Professor implements Comparable<Professor>, Serializable {

    private final     String       firstName;
    private final     String       lastName;
    private           Long         id;
    private transient List<Course> courses = new ArrayList<>();

    public Professor(long id, String firstName, String lastName) {

        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void addCourse(Course course) {

        courses.add(course);
    }

    public void removeCourse(Course course) {

        courses.remove(course);
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
