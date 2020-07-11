package com.foxminded.timetable.forms;

import com.foxminded.timetable.forms.constraints.Date;
import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDate;

@Data
public class RescheduleForm {

    @Min(value = 1, message = "Option ID must not be less than 1")
    private long optionId;

    @Min(value = 1, message = "Schedule ID must not be less than 1")
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
