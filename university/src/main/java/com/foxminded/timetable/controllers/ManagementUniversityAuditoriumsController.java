package com.foxminded.timetable.controllers;

import com.foxminded.timetable.forms.NewItemForm;
import com.foxminded.timetable.forms.RenameForm;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateAuditoriumId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("**/university/auditoriums")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ManagementUniversityAuditoriumsController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableFacade timetableFacade;

    @GetMapping("/")
    public String auditoriums(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Auditorium> auditoriums = timetableFacade.getAuditoriums();
        model.addAttribute("auditoriums", auditoriums);

        RenameForm renameForm = new RenameForm();
        model.addAttribute("renameForm", renameForm);

        NewItemForm newItemForm = new NewItemForm();
        model.addAttribute("newItemForm", newItemForm);

        return "management/university/auditoriums/auditoriums";
    }

    @PostMapping("/schedule")
    public String auditoriumSchedule(Model model,
            @ModelAttribute @Valid ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Optional<Auditorium> optionalAuditorium =
                timetableFacade.getAuditorium(scheduleForm.getId());
        if (!optionalAuditorium.isPresent()) {
            log.error("Auditorium with ID({}) no found", scheduleForm.getId());
            redirectAttributes.addFlashAttribute("errorAlert", "Attempt to "
                    + "locate schedule failed: auditorium with ID("
                    + scheduleForm.getId()
                    + ") could not be found. Please, resubmit the form.");
            return "redirect:/timetable/management/university/auditoriums";
        }
        Auditorium auditorium = optionalAuditorium.get();

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
                                new SchedulePredicateAuditoriumId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/auditoriums/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateAuditoriumId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/auditoriums/schedule/month";

            default:
                return "redirect:/timetable/management/university/auditoriums";
        }
    }

    @GetMapping("/schedule")
    public String redirectAuditoriums() {

        return "redirect:/timetable/management/university/auditoriums";
    }

    @PostMapping("/rename")
    public String rename(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid RenameForm renameForm) {

        Optional<Auditorium> optionalAuditorium =
                timetableFacade.getAuditorium(renameForm.getRenameId());
        if (!optionalAuditorium.isPresent()) {
            log.error("Auditorium with ID({}) no found",
                    renameForm.getRenameId());
            redirectAttributes.addFlashAttribute("errorAlert", "Attempt to "
                    + "rename auditorium failed: auditorium with ID("
                    + renameForm.getRenameId()
                    + ") could not be found. Please, resubmit the form.");
            return "redirect:/timetable/management/university/auditoriums";
        }
        Auditorium auditorium = optionalAuditorium.get();

        auditorium.setName(renameForm.getNewName());
        timetableFacade.saveAuditorium(auditorium);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Auditorium ID (%d) is now called %s",
                        auditorium.getId(), auditorium.getName()));
        redirectAttributes.addFlashAttribute("editedId", auditorium.getId());

        return "redirect:/timetable/management/university/auditoriums";
    }

    @GetMapping("/remove")
    public String removeAuditorium(RedirectAttributes redirectAttributes,
            @Min(value = 1,
                 message = "Auditorium ID must not be less than 1") @RequestParam(
                    "id") long id) {

        Optional<Auditorium> optionalAuditorium =
                timetableFacade.getAuditorium(id);
        if (!optionalAuditorium.isPresent()) {
            log.error("Auditorium with ID({}) no found", id);
            redirectAttributes.addFlashAttribute("errorAlert", "Attempt to "
                    + "remove auditorium failed: auditorium with ID(" + id
                    + ") could not be found. Please, double-check and "
                    + "resubmit.");
            return "redirect:/timetable/management/university/auditoriums";
        }
        Auditorium auditorium = optionalAuditorium.get();

        timetableFacade.deleteAuditorium(auditorium);

        redirectAttributes.addFlashAttribute("successAlert",
                "Auditorium ID (" + id + ") was deleted");
        redirectAttributes.addFlashAttribute("editedId", id);

        return "redirect:/timetable/management/university/auditoriums";
    }

    @PostMapping("/new")
    public String addNewAuditorium(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid NewItemForm newItemForm) {

        Auditorium auditorium = new Auditorium(newItemForm.getName());
        auditorium = timetableFacade.saveAuditorium(auditorium);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Auditorium %s with ID (%d) added to the list",
                        auditorium.getName(), auditorium.getId()));
        redirectAttributes.addFlashAttribute("editedId", auditorium.getId());


        return "redirect:/timetable/management/university/auditoriums";
    }

    @ExceptionHandler(BindException.class)
    public String handleInvalidData(RedirectAttributes redirectAttributes,
            BindException exception) {

        log.warn(exception.getMessage());
        String errorAlert = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        redirectAttributes.addFlashAttribute("errorAlert", errorAlert);

        return "redirect:/timetable/management/university/auditoriums";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleInvalidData(RedirectAttributes redirectAttributes,
            ConstraintViolationException exception) {

        log.warn(exception.getMessage());
        String errorAlert = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        redirectAttributes.addFlashAttribute("errorAlert", errorAlert);

        return "redirect:/timetable/management/university/auditoriums";
    }

}
