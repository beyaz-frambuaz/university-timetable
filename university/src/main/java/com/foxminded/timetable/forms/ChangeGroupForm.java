package com.foxminded.timetable.forms;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.model.Group;
import lombok.Data;

import java.util.List;

@Data
public class ChangeGroupForm {

    @IdValid("Group")
    private long newGroupId;

    @IdValid("Student")
    private long studentId;

    private List<Group> groups;

}
