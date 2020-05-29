package com.foxminded.timetable.web.forms;

import lombok.Data;

@Data
public class ChangeScheduleForm {

    private Long scheduleId;
    private Long auditoriumId;
    private Long professorId;
}

