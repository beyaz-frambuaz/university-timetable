package com.foxminded.timetable.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professors")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Professor implements Comparable<Professor>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "p_seq")
    @SequenceGenerator(name = "p_seq", sequenceName = "professor_id_seq")
    private Long id;

    private String firstName;

    private String lastName;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "professors_courses")
    private Set<Course> courses = new HashSet<>();

    public Professor(String firstName, String lastName) {

        this.firstName = firstName;
        this.lastName = lastName;
    }

    public Professor(long id, String firstName, String lastName) {

        this(firstName, lastName);
        this.id = id;
    }

    public Set<Course> getCourses() {

        return this.courses;
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
