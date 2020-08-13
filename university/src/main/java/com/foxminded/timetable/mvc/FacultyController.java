package com.foxminded.timetable.mvc;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.SessionExpiredException;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateProfessorId;
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
@RequestMapping("/timetable/faculty")
@Validated
@RequiredArgsConstructor
@Slf4j
public class FacultyController {

    private final TimetableFacade timetableFacade;
    private final ScheduleFormatter scheduleFormatter;

    @GetMapping("/list")
    public String listProfessors(Model model, HttpSession session,
            @ModelAttribute("sessionExpired") String message,
            @ModelAttribute("errorAlert") String errorAlert) {

        session.removeAttribute("student");
        session.removeAttribute("professor");

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("sessionExpired", message);
        List<Professor> professors = timetableFacade.getProfessors();
        model.addAttribute("professors", professors);

        return "faculty/list";
    }

    @PostMapping("/list")
    public String selectProfessor(
            @RequestParam("professorId") @IdValid("Professor") long professorId,
            HttpSession session, RedirectAttributes redirectAttributes) {

        Optional<Professor> professor =
                timetableFacade.getProfessor(professorId);
        if (!professor.isPresent()) {
            log.error("Professor with ID({}) not found", professorId);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Could not find professor with ID " + professorId
                            + "! Please, try again");
            return "redirect:/timetable/faculty/list";
        }

        session.setAttribute("professor", professor.get());

        return "redirect:/timetable/faculty/professor/home";
    }

    @GetMapping("/professor/home")
    public String home(HttpSession httpSession, Model model) {

        validateSession(httpSession);

        Professor professor = (Professor) httpSession.getAttribute("professor");

        LocalDate today = LocalDate.now();
        DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                new SchedulePredicateProfessorId(professor.getId()), today,
                true);
        model.addAttribute("daySchedule", daySchedule);

        return "faculty/professor/home";
    }

    @GetMapping("/professor/two_week")
    public String twoWeekSchedule(Model model, HttpSession httpSession) {

        validateSession(httpSession);

        TwoWeekSchedule twoWeekSchedule =
                scheduleFormatter.prepareTwoWeekSchedule();
        model.addAttribute("twoWeekSchedule", twoWeekSchedule);

        return "faculty/professor/schedule/two_week";
    }

    @GetMapping("/professor/schedule")
    public String redirectHome() {

        return "redirect:/timetable/faculty/professor/home";
    }

    @PostMapping("/professor/schedule")
    public String processForm(Model model, HttpSession httpSession,
            @ModelAttribute @Valid ScheduleForm scheduleForm) {

        validateSession(httpSession);

        Professor professor = (Professor) httpSession.getAttribute("professor");
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateProfessorId(professor.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "faculty/professor/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                                new SchedulePredicateProfessorId(
                                        professor.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "faculty/professor/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateProfessorId(
                                        professor.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "faculty/professor/schedule/month";

            default:
                return "redirect:/timetable/faculty/professor/home";
        }
    }

    @GetMapping("/professor/courses")
    public String courses(Model model, HttpSession httpSession) {

        validateSession(httpSession);

        Professor professor = (Professor) httpSession.getAttribute("professor");
        Map<Course, List<Student>> allCourseAttendees = new HashMap<>();
        for (Course course : professor.getCourses()) {
            allCourseAttendees.put(course,
                    timetableFacade.getCourseAttendees(course, professor));
        }
        model.addAttribute("allCourseAttendees", allCourseAttendees);

        return "faculty/professor/courses";
    }

    private void validateSession(HttpSession httpSession) {

        if (httpSession.getAttribute("professor") == null) {
            throw new SessionExpiredException(Professor.class);
        }
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

        return "redirect:/timetable/faculty/list";
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

        return "redirect:/timetable/faculty/list";
    }

}
