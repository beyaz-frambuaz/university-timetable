package com.shablii.timetable.forms;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.model.Group;
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
