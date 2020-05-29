package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.SemesterCalendar;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateNoFilter;
import com.foxminded.timetable.service.model.generator.DataGenerator;
import com.foxminded.timetable.web.exception.NotFoundException;
import com.foxminded.timetable.web.formatter.OptionsFormatter;
import com.foxminded.timetable.web.formatter.ScheduleFormatter;
import com.foxminded.timetable.web.forms.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/timetable/management")
public class ManagementController {

    private final ScheduleFormatter scheduleFormatter;
    private final OptionsFormatter  optionsFormatter;
    private final DataGenerator     dataGenerator;
    private final TimetableService  timetableService;
    private final SemesterCalendar  semesterCalendar;

    @GetMapping("/home")
    public String home(@ModelAttribute("successAlert") String successAlert,
            Model model, HttpSession session) {

        session.removeAttribute("student");
        session.removeAttribute("professor");

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
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm) {

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
        }

        return "error/404";
    }

    @GetMapping("/schedule/available")
    public String displayAvailableAuditoriumsProfessors(Model model,
            @RequestParam("scheduleId") long scheduleId,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("successAlert", successAlert);
        model.addAttribute("errorAlert", errorAlert);

        Schedule schedule;
        try {
            schedule = timetableService.getSchedule(scheduleId);
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }
        model.addAttribute("schedule", schedule);

        List<Professor> professors =
                timetableService.getAvailableProfessors(schedule.getDate(),
                        schedule.getPeriod());
        model.addAttribute("professors", professors);

        List<Auditorium> auditoriums =
                timetableService.getAvailableAuditoriums(schedule.getDate(),
                        schedule.getPeriod());
        model.addAttribute("auditoriums", auditoriums);

        ChangeScheduleForm changeScheduleForm = new ChangeScheduleForm();
        model.addAttribute("changeScheduleForm", changeScheduleForm);

        return "management/schedule/available";
    }

    @PostMapping("/schedule/available")
    public String changeSchedule(RedirectAttributes redirectAttributes,
            @ModelAttribute(
                    "changeScheduleForm") ChangeScheduleForm changeScheduleForm) {

        Schedule schedule;
        try {
            schedule = timetableService.getSchedule(
                    changeScheduleForm.getScheduleId());
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }

        if (changeScheduleForm.getAuditoriumId() != null) {
            Auditorium auditorium;
            try {
                auditorium = timetableService.getAuditorium(
                        changeScheduleForm.getAuditoriumId());
            } catch (ServiceException e) {
                redirectAttributes.addFlashAttribute("errorAlert",
                        e.getMessage());
                return "redirect:/timetable/management/schedule/available"
                        + "?scheduleId=" + schedule.getId();
            }
            schedule.setAuditorium(auditorium);
        }

        if (changeScheduleForm.getProfessorId() != null) {
            Professor professor;
            try {
                professor = timetableService.getProfessor(
                        changeScheduleForm.getProfessorId());
            } catch (ServiceException e) {
                redirectAttributes.addFlashAttribute("errorAlert",
                        e.getMessage());
                return "redirect:/timetable/management/schedule/available"
                        + "?scheduleId=" + schedule.getId();
            }
            schedule.setProfessor(professor);
        }

        timetableService.saveSchedule(schedule);
        redirectAttributes.addFlashAttribute("successAlert", "Schedule "
                + "updated, please see details in the description below");

        return "redirect:/timetable/management/schedule/available?scheduleId="
                + schedule.getId();
    }

    @PostMapping("/schedule/options")
    public String displayReschedulingOptions(Model model, @ModelAttribute(
            "findReschedulingOptionsForm") FindReschedulingOptionsForm findReschedulingOptionsForm) {

        Schedule schedule;
        try {
            schedule = timetableService.getSchedule(
                    findReschedulingOptionsForm.getScheduleId());
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }
        model.addAttribute("schedule", schedule);

        LocalDate date = findReschedulingOptionsForm.getLocalDate();
        WeekOptions weekOptions = null;
        if (findReschedulingOptionsForm.getScheduleOption()
                == ScheduleOption.DAY) {

            weekOptions =
                    optionsFormatter.prepareWeekOptions(schedule, date, date);
        }
        if (findReschedulingOptionsForm.getScheduleOption()
                == ScheduleOption.WEEK) {

            LocalDate monday = semesterCalendar.getWeekMonday(date);
            LocalDate friday = semesterCalendar.getWeekFriday(date);
            weekOptions = optionsFormatter.prepareWeekOptions(schedule, monday,
                    friday);
        }
        model.addAttribute("weekOptions", weekOptions);

        RescheduleForm rescheduleForm = new RescheduleForm();
        rescheduleForm.setRescheduleFormOption(RescheduleFormOption.ONCE);
        model.addAttribute("rescheduleForm", rescheduleForm);

        return "management/schedule/options";
    }

    @PostMapping("/schedule/reschedule")
    public String reschedule(Model model,
            @ModelAttribute("rescheduleForm") RescheduleForm rescheduleForm) {

        Schedule schedule;
        try {
            schedule = timetableService.getSchedule(
                    rescheduleForm.getScheduleId());
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }
        model.addAttribute("initial", new Schedule(schedule));

        ReschedulingOption option;
        try {
            option = timetableService.getReschedulingOption(
                    rescheduleForm.getOptionId());
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }

        String message = "";
        List<Schedule> affected = Collections.emptyList();
        if (rescheduleForm.getRescheduleFormOption()
                == RescheduleFormOption.ONCE) {

            message = String.format("Course %s for group %s "
                            + "with professor %s has been moved. See table "
                            + "below for details",
                    schedule.getCourse().getName(),
                    schedule.getGroup().getName(),
                    schedule.getProfessor().getFullName());
            affected = Collections.singletonList(
                    timetableService.rescheduleOnce(schedule,
                            rescheduleForm.getLocalDate(), option));
        }
        if (rescheduleForm.getRescheduleFormOption()
                == RescheduleFormOption.PERMANENTLY) {

            try {
                message = String.format("All by-weekly occurrences of course "
                                + "%s for group %s with professor %s have been "
                                + "moved. See table below for details",
                        schedule.getCourse().getName(),
                        schedule.getGroup().getName(),
                        schedule.getProfessor().getFullName());
                affected = timetableService.reschedulePermanently(schedule,
                        rescheduleForm.getLocalDate(), option);
            } catch (ServiceException e) {
                throw new NotFoundException(e.getMessage());
            }
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

}
