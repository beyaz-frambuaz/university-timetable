package com.foxminded.timetable.forms;

import com.foxminded.timetable.constraints.Date;
import com.foxminded.timetable.constraints.IdValid;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RescheduleForm {

    @IdValid("Option")
    private long optionId;

    @IdValid("Schedule")
    private long scheduleId;

    @Date
    private String date;

    private LocalDate localDate;

    private RescheduleFormOption rescheduleFormOption;

    public LocalDate getLocalDate() {

        if (this.localDate == null) {
            this.localDate = LocalDate.parse(date);
        }
        return this.localDate;
    }

}
