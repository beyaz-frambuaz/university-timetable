package com.shablii.timetable.dao;

import com.shablii.timetable.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

}
