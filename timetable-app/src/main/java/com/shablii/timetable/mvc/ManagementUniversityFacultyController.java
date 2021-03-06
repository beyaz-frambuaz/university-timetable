package com.shablii.timetable.mvc;

import com.shablii.timetable.constraints.IdValid;
import com.shablii.timetable.forms.*;
import com.shablii.timetable.forms.utility.*;
import com.shablii.timetable.forms.utility.formatter.ScheduleFormatter;
import com.shablii.timetable.model.*;
import com.shablii.timetable.service.TimetableFacade;
import com.shablii.timetable.service.utility.predicates.SchedulePredicateProfessorId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("**/university/faculty")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ManagementUniversityFacultyController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableFacade timetableFacade;

    @GetMapping("/")
    public String professors(Model model, @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId, @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Professor> professors = timetableFacade.getProfessors();
        model.addAttribute("professors", professors);

        NewProfessorForm newProfessorForm = new NewProfessorForm();
        model.addAttribute("newProfessorForm", newProfessorForm);

        return "management/university/faculty/professors";
    }

    @PostMapping("/schedule")
    public String professorSchedule(Model model, @ModelAttribute @Valid ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Optional<Professor> optionalProfessor = timetableFacade.getProfessor(scheduleForm.getId());
        if (!optionalProfessor.isPresent()) {
            log.error("Professor with ID({}) not found", scheduleForm.getId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to locate schedule failed: professor with ID(" + scheduleForm.getId()
                            + ") could not be found. Please, resubmit the " + "form.");
            return "redirect:/timetable/management/university/faculty";
        }
        Professor professor = optionalProfessor.get();

        model.addAttribute("professor", professor);
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateProfessorId(scheduleForm.getId()), date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "management/university/faculty/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule = scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateProfessorId(scheduleForm.getId()), date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/faculty/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule = scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateProfessorId(scheduleForm.getId()), date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/faculty/schedule/month";

            default:
                log.error("Faulty schedule form submitted: {}", scheduleForm);
                redirectAttributes.addFlashAttribute("errorAlert",
                        "Attempt to " + "locate schedule failed. We've logged the error and"
                                + " will resolve it ASAP. In the meantime, please " + "try to resubmit the form.");
                return "redirect:/timetable/management/university/faculty";
        }
    }

    @GetMapping("/schedule")
    public String redirectProfessors() {

        return "redirect:/timetable/management/university/faculty";
    }

    @GetMapping("/remove")
    public String removeProfessor(RedirectAttributes redirectAttributes,
            @RequestParam("id") @IdValid("Professor") long id) {

        Optional<Professor> optionalProfessor = timetableFacade.getProfessor(id);
        if (!optionalProfessor.isPresent()) {
            log.error("Professor with ID({}) no found", id);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to remove professor failed: professor with ID(" + id + ") could not be found. Please, "
                            + "double-check and resubmit.");
            return "redirect:/timetable/management/university/faculty";
        }
        Professor professor = optionalProfessor.get();

        timetableFacade.deleteProfessor(professor);

        redirectAttributes.addFlashAttribute("successAlert", "Professor ID (" + id + ") was deleted");
        redirectAttributes.addFlashAttribute("editedId", id);

        return "redirect:/timetable/management/university/faculty";
    }

    @PostMapping("/new")
    public String addNewProfessor(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid NewProfessorForm newProfessorForm) {

        Professor newProfessor = new Professor(newProfessorForm.getFirstName(), newProfessorForm.getLastName());
        newProfessor = timetableFacade.saveProfessor(newProfessor);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Professor %s with ID (%d) added", newProfessor.getFullName(), newProfessor.getId()));
        redirectAttributes.addFlashAttribute("editedId", newProfessor.getId());

        return "redirect:/timetable/management/university/faculty";
    }

    @GetMapping("/courses")
    public String courses(@RequestParam("professorId") @IdValid("Professor") long professorId,
            @ModelAttribute("successAlert") String successAlert, @ModelAttribute("errorAlert") String errorAlert,
            @ModelAttribute("editedId") String editedId, Model model, RedirectAttributes redirectAttributes) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        Optional<Professor> optionalProfessor = timetableFacade.getProfessor(professorId);
        if (!optionalProfessor.isPresent()) {
            log.error("Professor with ID({}) not found", professorId);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to locate professor's courses failed: professor " + "with ID(" + professorId
                            + ") could not be found" + ". Please, resubmit the form.");
            return "redirect:/timetable/management/university/faculty";
        }
        Professor professor = optionalProfessor.get();
        model.addAttribute("professor", professor);

        Map<Course, List<Student>> allCourseAttendees = new HashMap<>();
        for (Course course : professor.getCourses()) {
            allCourseAttendees.put(course, timetableFacade.getCourseAttendees(course, professor));
        }
        model.addAttribute("allCourseAttendees", allCourseAttendees);

        DropCourseForm dropCourseForm = new DropCourseForm();
        model.addAttribute("dropCourseForm", dropCourseForm);

        List<Course> newCourses = timetableFacade.getCourses();
        newCourses.removeAll(professor.getCourses());
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setNewCourses(newCourses);
        model.addAttribute("addCourseForm", addCourseForm);

        return "management/university/faculty/courses";
    }

    @PostMapping("/courses/add")
    public String addCourse(RedirectAttributes redirectAttributes, @ModelAttribute @Valid AddCourseForm addCourseForm) {

        Optional<Professor> optionalProfessor = timetableFacade.getProfessor(addCourseForm.getProfessorId());
        if (!optionalProfessor.isPresent()) {
            log.error("Professor with ID({}) not found", addCourseForm.getProfessorId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to add course failed: professor " + "with ID(" + addCourseForm.getProfessorId()
                            + ") could not be found. " + "Please, resubmit the form.");
            return "redirect:/timetable/management/university/faculty";
        }
        Professor professor = optionalProfessor.get();

        Optional<Course> optionalCourse = timetableFacade.getCourse(addCourseForm.getNewCourse());
        if (!optionalCourse.isPresent()) {
            log.error("Course with ID({}) not found", addCourseForm.getNewCourse());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to add course failed: course " + "with ID(" + addCourseForm.getNewCourse()
                            + ") could not be found. " + "Please, resubmit the form.");
            return "redirect:/timetable/management/university/faculty/courses" + "?professorId=" + professor.getId();
        }
        Course course = optionalCourse.get();

        professor.addCourse(course);
        timetableFacade.saveProfessor(professor);

        redirectAttributes.addFlashAttribute("editedId", course.getId());
        redirectAttributes.addFlashAttribute("successAlert", String.format("Course %s added", course.getName()));

        return "redirect:/timetable/management/university/faculty/courses" + "?professorId=" + professor.getId();
    }

    @PostMapping("/courses/drop")
    public String dropCourse(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid DropCourseForm dropCourseForm) {

        Optional<Professor> optionalProfessor = timetableFacade.getProfessor(dropCourseForm.getProfessorId());
        if (!optionalProfessor.isPresent()) {
            log.error("Professor with ID({}) not found", dropCourseForm.getProfessorId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to drop course failed: professor " + "with ID(" + dropCourseForm.getProfessorId()
                            + ") could not be found. " + "Please, resubmit the form.");
            return "redirect:/timetable/management/university/faculty";
        }
        Professor professor = optionalProfessor.get();

        Optional<Course> optionalCourse = timetableFacade.getCourse(dropCourseForm.getCourseId());
        if (!optionalCourse.isPresent()) {
            log.error("Course with ID({}) not found", dropCourseForm.getCourseId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to drop course failed: course " + "with ID(" + dropCourseForm.getCourseId()
                            + ") could not be found. " + "Please, resubmit the form.");
            return "redirect:/timetable/management/university/faculty/courses" + "?professorId=" + professor.getId();
        }
        Course course = optionalCourse.get();

        professor.removeCourse(course);
        timetableFacade.saveProfessor(professor);
        redirectAttributes.addFlashAttribute("successAlert", String.format("Course %s dropped", course.getName()));

        return "redirect:/timetable/management/university/faculty/courses" + "?professorId=" + professor.getId();
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

        return "redirect:/timetable/management/university/faculty";
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public String handleInvalidData(RedirectAttributes redirectAttributes, ConstraintViolationException exception) {

        log.warn(exception.getMessage());
        String errorAlert = exception.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        redirectAttributes.addFlashAttribute("errorAlert", errorAlert);

        return "redirect:/timetable/management/university/faculty";
    }

}
