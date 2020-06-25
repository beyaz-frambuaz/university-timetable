package com.foxminded.timetable.dao.jpa;

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;
import org.springframework.stereotype.Repository;

@Repository
public class CourseDaoImpl extends GenericDaoImpl<Course> implements CourseDao {

    public CourseDaoImpl() {

        super(Course.class);
    }

}
