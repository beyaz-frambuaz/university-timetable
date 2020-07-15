package com.foxminded.timetable.forms;

import com.foxminded.timetable.constraints.IdValid;
import lombok.Data;

@Data
public class DropCourseForm {

    @IdValid("Professor")
    private long professorId;

    @IdValid("Course")
    private long courseId;

}
