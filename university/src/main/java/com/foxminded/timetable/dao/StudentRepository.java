package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByGroupIn(List<Group> groups);

}
