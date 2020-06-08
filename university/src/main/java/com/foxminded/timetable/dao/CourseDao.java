package com.foxminded.timetable.dao;

import com.foxminded.timetable.model.Course;

public interface CourseDao extends GenericDao<Course> {

    Course save(Course course);

    Course update(Course course);

}