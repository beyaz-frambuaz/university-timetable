package com.foxminded.timetable.forms;

import com.foxminded.timetable.model.Course;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class AddCourseForm {

    private List<Course> newCourses;

    @Min(value = 1, message = "New course ID must not be less than 1")
    private long newCourse;

    @Min(value = 1, message = "Professor ID must not be less than 1")
    private long professorId;

}
