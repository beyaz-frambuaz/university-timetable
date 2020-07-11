package com.foxminded.timetable.forms;

import com.foxminded.timetable.model.Group;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class NewStudentForm {

    @NotBlank(message = "First name required, at least one character")
    private String firstName;

    @NotBlank(message = "Last name required, at least one character")
    private String lastName;

    @Min(value = 1, message = "Group ID must not be less than 1")
    private long groupId;

    private List<Group> groups;

}
