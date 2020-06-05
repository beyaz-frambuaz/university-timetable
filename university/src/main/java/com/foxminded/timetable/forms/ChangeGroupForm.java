package com.foxminded.timetable.forms;

import com.foxminded.timetable.model.Group;
import lombok.Data;

import java.util.List;

@Data
public class ChangeGroupForm {

    private long        newGroupId;
    private long        studentId;
    private List<Group> groups;

}
