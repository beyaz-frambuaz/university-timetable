package com.foxminded.timetable.forms;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NewProfessorForm {

    @NotBlank(message = "First name required, at least one character")
    private String firstName;

    @NotBlank(message = "Last name required, at least one character")
    private String lastName;

}
