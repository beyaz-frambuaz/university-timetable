package com.foxminded.timetable.forms;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class DropCourseForm {

    @Min(value = 1, message = "Professor ID must not be less than 1")
    private long professorId;

    @Min(value = 1, message = "Course ID must not be less than 1")
    private long courseId;

}
