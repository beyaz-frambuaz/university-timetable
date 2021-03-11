package com.shablii.timetable.forms;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class NewItemForm {

    @NotBlank(message = "Name required, at least one character")
    private String name;

}
