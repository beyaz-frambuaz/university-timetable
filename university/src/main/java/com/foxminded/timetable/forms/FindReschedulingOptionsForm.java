package com.foxminded.timetable.forms;

import com.foxminded.timetable.constraints.Date;
import com.foxminded.timetable.constraints.IdValid;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FindReschedulingOptionsForm {

    @Date
    private String date;

    private LocalDate localDate;

    @IdValid("Schedule")
    private long scheduleId;

    private ScheduleOption scheduleOption;

    public LocalDate getLocalDate() {

        if (this.localDate == null) {
            this.localDate = LocalDate.parse(date);
        }
        return this.localDate;
    }

}




