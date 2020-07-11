package com.foxminded.timetable.forms;

import com.foxminded.timetable.forms.constraints.Date;
import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDate;

@Data
public class FindReschedulingOptionsForm {

    @Date
    private String date;

    private LocalDate localDate;

    @Min(value = 1, message = "Schedule ID must not be less than 1")
    private long scheduleId;

    private ScheduleOption scheduleOption;

    public LocalDate getLocalDate() {

        if (this.localDate == null) {
            this.localDate = LocalDate.parse(date);
        }
        return this.localDate;
    }

}




