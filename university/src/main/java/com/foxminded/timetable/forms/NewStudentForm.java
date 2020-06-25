package com.foxminded.timetable.forms;

import com.foxminded.timetable.model.Group;
import lombok.Data;

import java.util.List;

@Data
public class NewStudentForm {

    private String firstName;
    private String lastName;
    private long groupId;
    private List<Group> groups;

}
