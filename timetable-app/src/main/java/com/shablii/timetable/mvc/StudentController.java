package com.shablii.timetable.mvc;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.exceptions.SessionExpiredException;
import com.shablii.timetable.forms.ScheduleForm;
import com.shablii.timetable.forms.utility.*;
import com.shablii.timetable.forms.utility.formatter.ScheduleFormatter;
import com.shablii.timetable.model.Student;
import com.shablii.timetable.service.TimetableFacade;
import com.shablii.timetable.service.utility.predicates.SchedulePredicateGroupId;
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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/timetable/students")
@Validated
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final TimetableFacade timetableFacade;
    private final ScheduleFormatter scheduleFormatter;

    @GetMapping("/list")
    public String listStudents(Model model, HttpSession session, @ModelAttribute("sessionExpired") String message,
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
    public String selectStudent(HttpSession session, @RequestParam("studentId") @IdValid("Student") long studentId,
            RedirectAttributes redirectAttributes) {

        Optional<Student> student = timetableFacade.getStudent(studentId);
        if (!student.isPresent()) {
            log.error("Student with ID({}) not found", studentId);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Could not find student with ID " + studentId + "! Please, try again");
            return "redirect:/timetable/students/list";
        }

        session.setAttribute("student", student.get());

        return "redirect:/timetable/students/student/home";
    }

    @GetMapping("/student/home")
    public String home(Model model, HttpSession httpSession) {

        validateSession(httpSession);

        Student student = (Student) httpSession.getAttribute("student");

        LocalDate today = LocalDate.now();
        DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                new SchedulePredicateGroupId(student.getGroup().getId()), today, true);
        model.addAttribute("daySchedule", daySchedule);

        return "students/student/home";
    }

    @GetMapping("/student/two_week")
    public String twoWeekSchedule(Model model, HttpSession httpSession) {

        validateSession(httpSession);

        TwoWeekSchedule twoWeekSchedule = scheduleFormatter.prepareTwoWeekSchedule();
        model.addAttribute("twoWeekSchedule", twoWeekSchedule);

        return "students/student/schedule/two_week";
    }

    @GetMapping("/student/schedule")
    public String redirectHome() {

        return "redirect:/timetable/students/student/home";
    }

    @PostMapping("/student/schedule")
    public String processForm(Model model, HttpSession httpSession, @ModelAttribute @Valid ScheduleForm scheduleForm) {

        validateSession(httpSession);

        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()), date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "students/student/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule = scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()), date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "students/student/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule = scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()), date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "students/student/schedule/month";

            default:
                return "redirect:/timetable/students/student/home";
        }
    }

    private void validateSession(HttpSession httpSession) {

        if (httpSession.getAttribute("student") == null) {
            throw new SessionExpiredException(Student.class);
        }
    }

    @ExceptionHandler(BindException.class)
    public String handleInvalidData(RedirectAttributes redirectAttributes, BindException exception) {

        log.warn(exception.getMessage());
        String errorAlert = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        redirectAttributes.addFlashAttribute("errorAlert", errorAlert);

        return "redirect:/timetable/students/list";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleInvalidData(RedirectAttributes redirectAttributes, ConstraintViolationException exception) {

        log.warn(exception.getMessage());
        String errorAlert = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        redirectAttributes.addFlashAttribute("errorAlert", errorAlert);

        return "redirect:/timetable/students/list";
    }

}
