package com.foxminded.timetable.forms;

import com.foxminded.timetable.forms.constraints.Date;
import lombok.Data;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Data
public class ScheduleForm {

    private ScheduleOption scheduleOption;

    @Date
    private String date;

    private LocalDate localDate;

    private boolean filtered;

    @Min(value = 1, message = "ID must not be less than 1")
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
