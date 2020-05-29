package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateProfessorId;
import com.foxminded.timetable.web.formatter.ScheduleFormatter;
import com.foxminded.timetable.web.forms.AddCourseForm;
import com.foxminded.timetable.web.forms.DropCourseForm;
import com.foxminded.timetable.web.forms.NewProfessorForm;
import com.foxminded.timetable.web.forms.ScheduleForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("**/university/faculty")
public class ManagementUniversityFacultyController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableService  timetableService;

    @GetMapping("/")
    public String professors(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Professor> professors = timetableService.getProfessors();
        model.addAttribute("professors", professors);

        NewProfessorForm newProfessorForm = new NewProfessorForm();
        model.addAttribute("newProfessorForm", newProfessorForm);

        return "management/university/faculty/professors";
    }

    @PostMapping("/schedule")
    public String professorSchedule(Model model,
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Professor professor;
        try {
            professor = timetableService.getProfessor(scheduleForm.getId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/management/university/faculty";
        }

        model.addAttribute("professor", professor);
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateProfessorId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "management/university/faculty/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateProfessorId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/faculty/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateProfessorId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/faculty/schedule/month";
        }

        return "error/404";
    }

    @GetMapping("/schedule")
    public String redirectProfessors() {

        return "redirect:/timetable/management/university/faculty";
    }

    @PostMapping("/new")
    public String addNewProfessor(RedirectAttributes redirectAttributes,
            @ModelAttribute(
                    "newProfessorForm") NewProfessorForm newProfessorForm) {

        Professor newProfessor = new Professor(newProfessorForm.getFirstName(),
                newProfessorForm.getLastName());
        newProfessor = timetableService.saveProfessor(newProfessor);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Professor %s with ID (%d) added",
                        newProfessor.getFullName(), newProfessor.getId()));
        redirectAttributes.addFlashAttribute("editedId", newProfessor.getId());

        return "redirect:/timetable/management/university/faculty";
    }

    @GetMapping("/courses")
    public String courses(@RequestParam("professorId") long professorId,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId, Model model,
            RedirectAttributes redirectAttributes) {

        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        Professor professor;
        try {
            professor = timetableService.getProfessor(professorId);
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/faculty";
        }
        model.addAttribute("professor", professor);

        Map<Course, List<Student>> allCourseAttendees = new HashMap<>();
        for (Course course : professor.getCourses()) {
            allCourseAttendees.put(course,
                    timetableService.getCourseAttendees(course, professor));
        }
        model.addAttribute("allCourseAttendees", allCourseAttendees);

        DropCourseForm dropCourseForm = new DropCourseForm();
        model.addAttribute("dropCourseForm", dropCourseForm);

        List<Course> newCourses = timetableService.getCourses();
        newCourses.removeAll(professor.getCourses());
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setNewCourses(newCourses);
        model.addAttribute("addCourseForm", addCourseForm);

        return "management/university/faculty/courses";
    }

    @PostMapping("/courses/add")
    public String addCourse(RedirectAttributes redirectAttributes,
            @ModelAttribute("addCourseForm") AddCourseForm addCourseForm) {

        Professor professor;
        try {
            professor = timetableService.getProfessor(
                    addCourseForm.getProfessorId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/faculty";
        }

        Course course;
        try {
            course = timetableService.getCourse(addCourseForm.getNewCourse());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/faculty";
        }

        professor.addCourse(course);

        timetableService.saveProfessor(professor);

        redirectAttributes.addFlashAttribute("editedId", course.getId());
        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Course %s added", course.getName()));

        return "redirect:/timetable/management/university/faculty/courses"
                + "?professorId=" + professor.getId();
    }

    @PostMapping("/courses/drop")
    public String dropCourse(RedirectAttributes redirectAttributes,
            @ModelAttribute("dropCourseForm") DropCourseForm dropCourseForm) {

        Professor professor;
        try {
            professor = timetableService.getProfessor(
                    dropCourseForm.getProfessorId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/faculty";
        }

        Course course;
        try {
            course = timetableService.getCourse(dropCourseForm.getCourseId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/faculty";
        }

        professor.removeCourse(course);

        timetableService.saveProfessor(professor);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Course %s dropped", course.getName()));

        return "redirect:/timetable/management/university/faculty/courses"
                + "?professorId=" + professor.getId();
    }

}
