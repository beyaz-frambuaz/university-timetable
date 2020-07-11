package com.foxminded.timetable.forms;

import com.foxminded.timetable.model.Group;
import lombok.Data;

import javax.validation.constraints.Min;
import java.util.List;

@Data
public class ChangeGroupForm {

    @Min(value = 1, message = "New group ID must not be less than 1")
    private long newGroupId;

    @Min(value = 1, message = "Student ID must not be less than 1")
    private long studentId;

    private List<Group> groups;

}
