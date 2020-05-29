package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateProfessorId;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/timetable/faculty")
public class FacultyController {

    private final TimetableService  timetableService;
    private final ScheduleFormatter scheduleFormatter;

    @GetMapping("/list")
    public String listProfessors(Model model, HttpSession session,
            @ModelAttribute("sessionExpired") String message) {

        session.removeAttribute("student");
        session.removeAttribute("professor");

        model.addAttribute("sessionExpired", message);
        List<Professor> professors = timetableService.getProfessors();
        model.addAttribute("professors", professors);

        return "faculty/list";
    }

    @PostMapping("/list")
    public String selectProfessor(@RequestParam("professorId") long professorId,
            HttpSession session) {

        Professor professor;
        try {
            professor = timetableService.getProfessor(professorId);
        } catch (ServiceException e) {
            throw new NotFoundException(e.getMessage());
        }

        session.setAttribute("professor", professor);

        return "redirect:/timetable/faculty/professor/home";
    }

    @GetMapping("/professor/home")
    public String home(HttpSession httpSession, Model model) {

        if (httpSession.getAttribute("professor") == null) {
            throw new SessionExpiredException(Professor.class);
        }
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

        if (httpSession.getAttribute("professor") == null) {
            throw new SessionExpiredException(Professor.class);
        }

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
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm) {

        if (httpSession.getAttribute("professor") == null) {
            throw new SessionExpiredException(Professor.class);
        }

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
                        new SchedulePredicateProfessorId(professor.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "faculty/professor/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateProfessorId(professor.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "faculty/professor/schedule/month";
        }

        return "error/404";
    }

    @GetMapping("/professor/courses")
    public String courses(Model model, HttpSession httpSession) {

        if (httpSession.getAttribute("professor") == null) {
            throw new SessionExpiredException(Professor.class);
        }

        Professor professor = (Professor) httpSession.getAttribute("professor");
        Map<Course, List<Student>> allCourseAttendees = new HashMap<>();
        for (Course course : professor.getCourses()) {
            allCourseAttendees.put(course,
                    timetableService.getCourseAttendees(course, professor));
        }
        model.addAttribute("allCourseAttendees", allCourseAttendees);

        return "faculty/professor/courses";
    }

}
