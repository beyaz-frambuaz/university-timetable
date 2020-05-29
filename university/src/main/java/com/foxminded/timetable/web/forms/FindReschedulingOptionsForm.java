package com.foxminded.timetable.web.forms;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FindReschedulingOptionsForm {

    private ScheduleOption scheduleOption;
    private String         date;
    private LocalDate      localDate;
    private long           scheduleId;

    public void setDate(String date) {

        this.date = date;
        this.localDate = LocalDate.parse(date);
    }

}




