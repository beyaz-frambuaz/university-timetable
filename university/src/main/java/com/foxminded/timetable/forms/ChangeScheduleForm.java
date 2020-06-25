package com.foxminded.timetable.forms;

import lombok.Data;

@Data
public class ChangeScheduleForm {

    private Long scheduleId;
    private Long auditoriumId;
    private Long professorId;

}

