package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateGroupId;
import com.foxminded.timetable.web.exception.NotFoundException;
import com.foxminded.timetable.web.exception.SessionExpiredException;
import com.foxminded.timetable.web.formatter.ScheduleFormatter;
import com.foxminded.timetable.web.forms.ScheduleForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/timetable/students")
public class StudentController {

    private final TimetableService  timetableService;
    private final ScheduleFormatter scheduleFormatter;

    @GetMapping("/list")
    public String listStudents(Model model, HttpSession session,
            @ModelAttribute("sessionExpired") String message) {

        session.removeAttribute("student");
        session.removeAttribute("professor");

        model.addAttribute("sessionExpired", message);
        List<Student> students = timetableService.getStudents();
        model.addAttribute("students", students);

        return "students/list";
    }

    @PostMapping("/list")
    public String selectStudent(@RequestParam("studentId") long studentId,
            HttpSession session) {

        Student student;
        try {
            student = timetableService.getStudent(studentId);
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }

        session.setAttribute("student", student);

        return "redirect:/timetable/students/student/home";
    }

    @GetMapping("/student/home")
    public String home(Model model, HttpSession httpSession) {

        if (httpSession.getAttribute("student") == null) {
            throw new SessionExpiredException(Student.class);
        }
        Student student = (Student) httpSession.getAttribute("student");

        LocalDate today = LocalDate.now();
        DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                new SchedulePredicateGroupId(student.getGroup().getId()), today,
                true);
        model.addAttribute("daySchedule", daySchedule);

        return "students/student/home";
    }

    @GetMapping("/student/two_week")
    public String twoWeekSchedule(Model model, HttpSession httpSession) {

        if (httpSession.getAttribute("student") == null) {
            throw new SessionExpiredException(Student.class);
        }

        TwoWeekSchedule twoWeekSchedule =
                scheduleFormatter.prepareTwoWeekSchedule();
        model.addAttribute("twoWeekSchedule", twoWeekSchedule);

        return "students/student/schedule/two_week";
    }

    @GetMapping("/student/schedule")
    public String redirectHome() {

        return "redirect:/timetable/students/student/home";
    }

    @PostMapping("/student/schedule")
    public String processForm(Model model, HttpSession httpSession,
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm) {

        if (httpSession.getAttribute("student") == null) {
            throw new SessionExpiredException(Student.class);
        }

        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "students/student/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "students/student/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "students/student/schedule/month";
        }

        return "error/404";
    }

}
