package com.foxminded.timetable.forms;

import com.foxminded.timetable.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.*;

@Data
public class ScheduleForm {

    private ScheduleOption scheduleOption;

    @Date
    private String date;

    private LocalDate localDate;

    private boolean filtered;

    @IdValid
    private long id;

    public String getDateDescription() {

        return getLocalDate().format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
    }

    public LocalDate getLocalDate() {

        if (this.localDate == null) {
            this.localDate = LocalDate.parse(date);
        }
        return this.localDate;
    }

}
