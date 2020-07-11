package com.foxminded.timetable.forms;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ChangeScheduleForm {

    @NotNull
    @Min(value = 1, message = "Schedule ID must not be less than 1")
    private Long scheduleId;

    @Min(value = 1, message = "Auditorium ID must not be less than 1")
    private Long auditoriumId;

    @Min(value = 1, message = "Professor ID must not be less than 1")
    private Long professorId;

}

