package com.foxminded.timetable.web.forms;

import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Data
public class ScheduleForm {

    private ScheduleOption scheduleOption;
    private String         date;
    private LocalDate      localDate;
    private boolean        filtered;
    private long           id;

    public String getDateDescription() {

        return localDate.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public void setDate(String date) {

        this.date = date;
        this.localDate = LocalDate.parse(date);
    }

}
