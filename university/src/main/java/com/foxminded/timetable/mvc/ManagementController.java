package com.foxminded.timetable.mvc;

import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.*;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.model.generator.DataGenerator;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateNoFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.*;
import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/timetable/management")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ManagementController {

    private final ScheduleFormatter scheduleFormatter;
    private final OptionsFormatter optionsFormatter;
    private final DataGenerator dataGenerator;
    private final TimetableFacade timetableFacade;
    private final SemesterCalendar semesterCalendar;

    @GetMapping("/home")
    public String home(@ModelAttribute("successAlert") String successAlert,
            Model model, HttpSession session,
            @ModelAttribute("errorAlert") String errorAlert) {

        session.removeAttribute("student");
        session.removeAttribute("professor");

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);

        LocalDate today = LocalDate.now();
        DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                new SchedulePredicateNoFilter(), today, false);
        model.addAttribute("daySchedule", daySchedule);

        return "management/home";
    }

    @GetMapping("/two_week")
    public String twoWeekSchedule(Model model) {

        TwoWeekSchedule twoWeekSchedule =
                scheduleFormatter.prepareTwoWeekSchedule();
        model.addAttribute("twoWeekSchedule", twoWeekSchedule);

        return "management/schedule/two_week";
    }

    @GetMapping({ "/schedule", "/schedule/reschedule", "/schedule/options" })
    public String redirectHome() {

        return "redirect:/timetable/management/home";
    }

    @PostMapping("/schedule")
    public String processForm(Model model,
            @ModelAttribute @Valid ScheduleForm scheduleForm) {

        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateNoFilter(), date, false);
                model.addAttribute("daySchedule", daySchedule);

                return "management/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                                new SchedulePredicateNoFilter(), date, false);
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateNoFilter(), date, false);
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/schedule/month";
            default:
                return "redirect:/timetable/management/home";
        }
    }

    @GetMapping("/schedule/available")
    public String displayAvailableAuditoriumsProfessors(Model model,
            @RequestParam("scheduleId") @Min(value = 1,
                                             message = "Schedule ID must not "
                                                     + "be less than 1") long scheduleId,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("successAlert", successAlert);
        model.addAttribute("errorAlert", errorAlert);

        Optional<Schedule> optionalSchedule =
                timetableFacade.getSchedule(scheduleId);
        if (!optionalSchedule.isPresent()) {
            log.error("Schedule with ID({}) not found", scheduleId);
            throw new NotFoundException(
                    "Attempt to locate available auditoriums and "
                            + "professors failed: schedule with ID("
                            + scheduleId + ") could not be found.");
        }
        Schedule schedule = optionalSchedule.get();

        model.addAttribute("schedule", schedule);

        List<Professor> professors =
                timetableFacade.getAvailableProfessors(schedule.getDate(),
                        schedule.getPeriod());
        model.addAttribute("professors", professors);

        List<Auditorium> auditoriums =
                timetableFacade.getAvailableAuditoriums(schedule.getDate(),
                        schedule.getPeriod());
        model.addAttribute("auditoriums", auditoriums);

        ChangeScheduleForm changeScheduleForm = new ChangeScheduleForm();
        model.addAttribute("changeScheduleForm", changeScheduleForm);

        return "management/schedule/available";
    }

    @PostMapping("/schedule/available")
    public String changeSchedule(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid ChangeScheduleForm changeScheduleForm) {

        Optional<Schedule> optionalSchedule =
                timetableFacade.getSchedule(changeScheduleForm.getScheduleId());
        if (!optionalSchedule.isPresent()) {
            log.error("Schedule with ID({}) not found",
                    changeScheduleForm.getScheduleId());
            String message = String.format("Attempt to change "
                            + "auditorium and/or professor failed: schedule "
                            + "with ID(%d) could not be found.",
                    changeScheduleForm.getScheduleId());
            throw new NotFoundException(message);
        }
        Schedule schedule = optionalSchedule.get();

        if (changeScheduleForm.getAuditoriumId() != null) {
            Optional<Auditorium> optionalAuditorium =
                    timetableFacade.getAuditorium(
                            changeScheduleForm.getAuditoriumId());
            if (!optionalAuditorium.isPresent()) {
                log.error("Auditorium with ID({}) not found",
                        changeScheduleForm.getAuditoriumId());
                String errorAlert = String.format(
                        "Attempt to change auditorium failed: auditorium "
                                + "with ID(%d) could not be found. Please, "
                                + "submit the form again.",
                        changeScheduleForm.getAuditoriumId());
                redirectAttributes.addFlashAttribute("errorAlert", errorAlert);
                return "redirect:/timetable/management/schedule/available"
                        + "?scheduleId=" + schedule.getId();
            }

            schedule.setAuditorium(optionalAuditorium.get());
        }

        if (changeScheduleForm.getProfessorId() != null) {
            Optional<Professor> optionalProfessor =
                    timetableFacade.getProfessor(
                            changeScheduleForm.getProfessorId());
            if (!optionalProfessor.isPresent()) {
                log.error("Professor with ID({}) not found",
                        changeScheduleForm.getProfessorId());
                String errorAlert = String.format(
                        "Attempt to change professor failed: professor "
                                + "with ID(%d) could not be found. Please, "
                                + "submit the form again.",
                        changeScheduleForm.getProfessorId());
                redirectAttributes.addFlashAttribute("errorAlert", errorAlert);
                return "redirect:/timetable/management/schedule/available"
                        + "?scheduleId=" + schedule.getId();
            }

            schedule.setProfessor(optionalProfessor.get());
        }

        timetableFacade.saveSchedule(schedule);
        redirectAttributes.addFlashAttribute("successAlert", "Schedule "
                + "updated, please see details in the description below");

        return "redirect:/timetable/management/schedule/available?scheduleId="
                + schedule.getId();
    }

    @PostMapping("/schedule/options")
    public String displayReschedulingOptions(Model model,
            @ModelAttribute @Valid FindReschedulingOptionsForm findReschedulingOptionsForm) {

        Optional<Schedule> optionalSchedule = timetableFacade.getSchedule(
                findReschedulingOptionsForm.getScheduleId());
        if (!optionalSchedule.isPresent()) {
            log.error("Schedule with ID({}) not found",
                    findReschedulingOptionsForm.getScheduleId());
            throw new NotFoundException("Attempt to find rescheduling "
                    + "options failed: schedule with ID("
                    + findReschedulingOptionsForm.getScheduleId() + ") could "
                    + "not be found.");
        }
        Schedule schedule = optionalSchedule.get();
        model.addAttribute("schedule", schedule);

        LocalDate date = findReschedulingOptionsForm.getLocalDate();

        DayOptions dayOptions = null;
        if (findReschedulingOptionsForm.getScheduleOption()
                == ScheduleOption.DAY) {

            dayOptions = optionsFormatter.prepareDayOptions(schedule, date);
        }
        model.addAttribute("dayOptions", dayOptions);

        WeekOptions weekOptions = null;
        if (findReschedulingOptionsForm.getScheduleOption()
                == ScheduleOption.WEEK) {

            weekOptions = optionsFormatter.prepareWeekOptions(schedule,
                    semesterCalendar.getSemesterWeekNumber(date));
        }
        model.addAttribute("weekOptions", weekOptions);

        RescheduleForm rescheduleForm = new RescheduleForm();
        rescheduleForm.setRescheduleFormOption(RescheduleFormOption.ONCE);
        rescheduleForm.setDate("2020-01-01");
        model.addAttribute("rescheduleForm", rescheduleForm);

        return "management/schedule/options";
    }

    @PostMapping("/schedule/reschedule")
    public String reschedule(Model model,
            @ModelAttribute @Valid RescheduleForm rescheduleForm) {

        Optional<Schedule> optionalSchedule =
                timetableFacade.getSchedule(rescheduleForm.getScheduleId());
        if (!optionalSchedule.isPresent()) {
            log.error("Schedule with ID({}) not found",
                    rescheduleForm.getScheduleId());
            throw new NotFoundException(
                    "Attempt to reschedule failed: schedule with ID("
                            + rescheduleForm.getScheduleId() + ") could "
                            + "not be found.");
        }
        Schedule schedule = optionalSchedule.get();
        model.addAttribute("initial", new Schedule(schedule));

        Optional<ReschedulingOption> optionalOption =
                timetableFacade.getOption(rescheduleForm.getOptionId());
        if (!optionalOption.isPresent()) {
            log.error("Option with ID({}) not found",
                    rescheduleForm.getOptionId());
            throw new NotFoundException("Attempt to find rescheduling options "
                    + "failed: option with ID(" + rescheduleForm.getOptionId()
                    + ") could not be found.");
        }
        ReschedulingOption option = optionalOption.get();

        String message = "";
        List<Schedule> affected = Collections.emptyList();
        if (rescheduleForm.getRescheduleFormOption()
                == RescheduleFormOption.ONCE) {

            message = String.format("Course %s for group %s with professor "
                            + "%s has been moved. See table below for details",
                    schedule.getCourse().getName(),
                    schedule.getGroup().getName(),
                    schedule.getProfessor().getFullName());
            affected = Collections.singletonList(
                    timetableFacade.rescheduleSingle(schedule,
                            rescheduleForm.getLocalDate(), option));
        }
        if (rescheduleForm.getRescheduleFormOption()
                == RescheduleFormOption.PERMANENTLY) {

            message = String.format("All by-weekly occurrences of course "
                            + "%s for group %s with professor %s have been "
                            + "moved. See table below for details",
                    schedule.getCourse().getName(),
                    schedule.getGroup().getName(),
                    schedule.getProfessor().getFullName());
            affected = timetableFacade.rescheduleRecurring(schedule,
                    rescheduleForm.getLocalDate(), option);
        }
        model.addAttribute("affected", affected);
        model.addAttribute("message", message);

        return "management/schedule/reschedule";
    }

    @PostMapping("/rebuild-timetable")
    public String rebuildTimetable(RedirectAttributes redirectAttributes) {

        dataGenerator.refreshTimetableData();
        redirectAttributes.addFlashAttribute("successAlert",
                "Timetable data has been erased and generated afresh");

        return "redirect:/timetable/management/home";
    }

    @PostMapping("/refresh-all-data")
    public String refreshAllData(RedirectAttributes redirectAttributes) {

        dataGenerator.refreshAllData();
        redirectAttributes.addFlashAttribute("successAlert",
                "All university and timetable data has been erased and "
                        + "generated afresh");

        return "redirect:/timetable/management/home";
    }

    @ExceptionHandler(NotFoundException.class)
    public String handleNotFound(NotFoundException e,
            RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
        return "redirect:/timetable/management/home";
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

        return "redirect:/timetable/management/home";
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

        return "redirect:/timetable/management/home";
    }

}
