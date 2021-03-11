package com.shablii.timetable.dao;

import com.shablii.timetable.model.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByGroupIn(List<Group> groups);

}
