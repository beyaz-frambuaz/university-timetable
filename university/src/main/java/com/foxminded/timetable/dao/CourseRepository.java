package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

}
