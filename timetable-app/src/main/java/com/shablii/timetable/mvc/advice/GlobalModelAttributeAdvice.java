package com.shablii.timetable.mvc.advice;

import com.shablii.timetable.forms.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @Value("${university.semester.start.date}")
    private String defaultDate;

    @ModelAttribute("scheduleForm")
    public ScheduleForm addDefaultForm() {

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setDate(defaultDate);
        scheduleForm.setFiltered(true);
        return scheduleForm;
    }

    @ModelAttribute("findReschedulingOptionsForm")
    public FindReschedulingOptionsForm addFindReschedulingOptionsForm() {

        FindReschedulingOptionsForm form = new FindReschedulingOptionsForm();
        form.setScheduleOption(ScheduleOption.DAY);
        form.setDate(defaultDate);
        return form;
    }

    @ModelAttribute("rescheduleForm")
    public RescheduleForm addRescheduleForm() {

        RescheduleForm rescheduleForm = new RescheduleForm();
        rescheduleForm.setRescheduleFormOption(RescheduleFormOption.ONCE);
        rescheduleForm.setDate(defaultDate);
        return rescheduleForm;
    }

}
