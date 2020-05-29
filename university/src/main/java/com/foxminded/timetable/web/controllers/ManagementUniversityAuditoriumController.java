package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.DaySchedule;
import com.foxminded.timetable.model.MonthSchedule;
import com.foxminded.timetable.model.WeekSchedule;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateAuditoriumId;
import com.foxminded.timetable.web.formatter.ScheduleFormatter;
import com.foxminded.timetable.web.forms.NewItemForm;
import com.foxminded.timetable.web.forms.RenameForm;
import com.foxminded.timetable.web.forms.ScheduleForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("**/university/auditoriums")
public class ManagementUniversityAuditoriumController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableService  timetableService;

    @GetMapping("/")
    public String auditoriums(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Auditorium> auditoriums = timetableService.getAuditoriums();
        model.addAttribute("auditoriums", auditoriums);

        RenameForm renameForm = new RenameForm();
        model.addAttribute("renameForm", renameForm);

        NewItemForm newItemForm = new NewItemForm();
        model.addAttribute("newItemForm", newItemForm);

        return "management/university/auditoriums/auditoriums";
    }

    @PostMapping("/schedule")
    public String auditoriumSchedule(Model model,
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Auditorium auditorium;
        try {
            auditorium = timetableService.getAuditorium(scheduleForm.getId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/auditoriums";
        }
        model.addAttribute("auditorium", auditorium);
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateAuditoriumId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "management/university/auditoriums/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateAuditoriumId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/auditoriums/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateAuditoriumId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/auditoriums/schedule/month";
        }

        return "error/404";
    }

    @GetMapping("/schedule")
    public String redirectAuditoriums() {

        return "redirect:/timetable/management/university/auditoriums";
    }

    @PostMapping("/rename")
    public String rename(RedirectAttributes redirectAttributes,
            @ModelAttribute("renameForm") RenameForm renameForm) {

        Auditorium auditorium;
        try {
            auditorium = timetableService.getAuditorium(
                    renameForm.getRenameId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/auditoriums";
        }

        auditorium.setName(renameForm.getNewName());
        timetableService.saveAuditorium(auditorium);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Auditorium ID (%d) is now called %s",
                        auditorium.getId(), auditorium.getName()));
        redirectAttributes.addFlashAttribute("editedId", auditorium.getId());

        return "redirect:/timetable/management/university/auditoriums";
    }

    @PostMapping("/new")
    public String addNewAuditorium(RedirectAttributes redirectAttributes,
            @ModelAttribute("newItemForm") NewItemForm newItemForm) {

        Auditorium auditorium = new Auditorium(newItemForm.getName());
        auditorium = timetableService.saveAuditorium(auditorium);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Auditorium %s with ID (%d) added to the list",
                        auditorium.getName(), auditorium.getId()));
        redirectAttributes.addFlashAttribute("editedId", auditorium.getId());


        return "redirect:/timetable/management/university/auditoriums";
    }

}
