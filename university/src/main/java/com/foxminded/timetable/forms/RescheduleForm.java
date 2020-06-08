package com.foxminded.timetable.forms;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RescheduleForm {

    private RescheduleFormOption rescheduleFormOption;
    private long optionId;
    private long scheduleId;
    private String date;
    private LocalDate localDate;

    public void setDate(String date) {

        this.date = date;
        this.localDate = LocalDate.parse(date);
    }

}
