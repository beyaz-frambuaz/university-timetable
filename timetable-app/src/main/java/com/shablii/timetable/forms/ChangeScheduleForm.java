package com.shablii.timetable.forms;

import com.shablii.timetable.constraints.IdValid;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class ChangeScheduleForm {

    @NotNull
    @IdValid("Schedule")
    private Long scheduleId;

    @IdValid("Auditorium")
    private Long auditoriumId;

    @IdValid("Professor")
    private Long professorId;

}

