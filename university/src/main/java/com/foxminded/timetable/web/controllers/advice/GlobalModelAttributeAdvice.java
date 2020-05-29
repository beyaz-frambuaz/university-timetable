package com.foxminded.timetable.web.controllers.advice;

import com.foxminded.timetable.web.forms.*;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @ModelAttribute("scheduleForm")
    public ScheduleForm addDefaultForm() {

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(true);
        return scheduleForm;
    }

    @ModelAttribute("findReschedulingOptionsForm")
    public FindReschedulingOptionsForm addFindReschedulingOptionsForm() {

        FindReschedulingOptionsForm form = new FindReschedulingOptionsForm();
        form.setScheduleOption(ScheduleOption.DAY);
        return form;
    }

    @ModelAttribute("rescheduleForm")
    public RescheduleForm addRescheduleForm() {

        RescheduleForm rescheduleForm = new RescheduleForm();
        rescheduleForm.setRescheduleFormOption(RescheduleFormOption.ONCE);
        return rescheduleForm;
    }

}
