package com.shablii.timetable.forms;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.model.Group;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class NewStudentForm {

    @NotBlank(message = "First name required, at least one character")
    private String firstName;

    @NotBlank(message = "Last name required, at least one character")
    private String lastName;

    @IdValid("Group")
    private long groupId;

    private List<Group> groups;

}
