package com.foxminded.timetable.forms;

import com.foxminded.timetable.model.Course;
import lombok.Data;

import java.util.List;

@Data
public class AddCourseForm {

    private List<Course> newCourses;
    private long newCourse;
    private long professorId;
}
