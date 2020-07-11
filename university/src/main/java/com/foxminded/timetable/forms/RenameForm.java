package com.foxminded.timetable.forms;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class RenameForm {

    @NotBlank(message = "New name required, at least one character")
    private String newName;

    @Min(value = 1, message = "ID must not be less than 1")
    private long renameId;

}
