package com.shablii.timetable.forms;

import com.shablii.timetable.constraints.IdValid;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RenameForm {

    @NotBlank(message = "New name required, at least one character")
    private String newName;

    @IdValid
    private long renameId;

}
