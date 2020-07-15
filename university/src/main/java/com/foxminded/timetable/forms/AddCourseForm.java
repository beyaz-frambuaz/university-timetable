package com.foxminded.timetable.forms;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.model.Course;
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
