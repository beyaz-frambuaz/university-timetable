package com.shablii.timetable.forms;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.model.Course;
import lombok.Data;

import java.util.List;

@Data
public class AddCourseForm {

    private List<Course> newCourses;

    @IdValid("Course")
    private long newCourse;

    @IdValid("Professor")
    private long professorId;

}
