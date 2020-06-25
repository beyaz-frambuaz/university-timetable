package com.foxminded.timetable.controllers;

import com.foxminded.timetable.exceptions.SessionExpiredException;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.TwoWeekSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateGroupId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/timetable/students")
public class StudentController {

    private final TimetableFacade timetableFacade;
    private final ScheduleFormatter scheduleFormatter;

    @GetMapping("/list")
    public String listStudents(Model model, HttpSession session,
            @ModelAttribute("sessionExpired") String message,
            @ModelAttribute("errorAlert") String errorAlert) {

        session.removeAttribute("student");
        session.removeAttribute("professor");

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("sessionExpired", message);
        List<Student> students = timetableFacade.getStudents();
        model.addAttribute("students", students);

        return "students/list";
    }

    @PostMapping("/list")
    public String selectStudent(@RequestParam("studentId") long studentId,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Optional<Student> student = timetableFacade.getStudent(studentId);
        if (!student.isPresent()) {
            log.error("Student with ID({}) not found", studentId);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Could not find student with ID " + studentId
                            + "! Please, try again");
            return "redirect:/timetable/students/list";
        }

        session.setAttribute("student", student.get());

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
                                new SchedulePredicateGroupId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "students/student/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateGroupId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "students/student/schedule/month";

            default:
                return "redirect:/timetable/students/student/home";
        }
    }

}
