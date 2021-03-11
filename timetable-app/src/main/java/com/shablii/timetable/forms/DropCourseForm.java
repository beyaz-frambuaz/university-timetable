package com.shablii.timetable.forms;

import com.shablii.timetable.constraints.IdValid;
import lombok.Data;

@Data
public class DropCourseForm {

    @IdValid("Professor")
    private long professorId;

    @IdValid("Course")
    private long courseId;

}
