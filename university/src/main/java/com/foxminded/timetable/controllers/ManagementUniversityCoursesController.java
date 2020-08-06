package com.foxminded.timetable.controllers;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateCourseId;
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
@RequestMapping("**/university/courses")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ManagementUniversityCoursesController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableFacade timetableFacade;

    @GetMapping("/")
    public String courses(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Course> courses = timetableFacade.getCourses();
        model.addAttribute("courses", courses);

        RenameForm renameForm = new RenameForm();
        model.addAttribute("renameForm", renameForm);

        NewItemForm newItemForm = new NewItemForm();
        model.addAttribute("newItemForm", newItemForm);

        return "management/university/courses/courses";
    }

    @PostMapping("/schedule")
    public String courseSchedule(Model model,
            @ModelAttribute("scheduleForm") @Valid ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Optional<Course> optionalCourse =
                timetableFacade.getCourse(scheduleForm.getId());
        if (!optionalCourse.isPresent()) {
            log.error("Course with ID({}) no found", scheduleForm.getId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to locate schedule failed: course with ID("
                            + scheduleForm.getId()
                            + ") could not be found. Please, resubmit the "
                            + "form.");
            return "redirect:/timetable/management/university/courses";
        }
        Course course = optionalCourse.get();
        model.addAttribute("course", course);
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateCourseId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "management/university/courses/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                                new SchedulePredicateCourseId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/courses/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateCourseId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/courses/schedule/month";

            default:
                log.error("Faulty schedule form submitted: {}", scheduleForm);
                redirectAttributes.addFlashAttribute("errorAlert", "Attempt to "
                        + "locate schedule failed. We've logged the error and"
                        + " will resolve it ASAP. In the meantime, please "
                        + "try to resubmit the form.");
                return "redirect:/timetable/management/university/courses";
        }
    }

    @GetMapping("/schedule")
    public String redirectCourses() {

        return "redirect:/timetable/management/university/courses";
    }

    @PostMapping("/rename")
    public String rename(RedirectAttributes redirectAttributes,
            @ModelAttribute("renameForm") @Valid RenameForm renameForm) {

        Optional<Course> optionalCourse =
                timetableFacade.getCourse(renameForm.getRenameId());
        if (!optionalCourse.isPresent()) {
            log.error("Course with ID({}) no found", renameForm.getRenameId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to rename course failed: course with ID("
                            + renameForm.getRenameId()
                            + ") could not be found. Please, resubmit the "
                            + "form.");
            return "redirect:/timetable/management/university/courses";
        }
        Course course = optionalCourse.get();

        course.setName(renameForm.getNewName());
        timetableFacade.saveCourse(course);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Course ID (%d) is now called %s", course.getId(),
                        course.getName()));
        redirectAttributes.addFlashAttribute("editedId", course.getId());

        return "redirect:/timetable/management/university/courses";
    }

    @GetMapping("/remove")
    public String removeCourse(RedirectAttributes redirectAttributes,
            @RequestParam("id") @IdValid("Course") long id) {

        Optional<Course> optionalCourse = timetableFacade.getCourse(id);
        if (!optionalCourse.isPresent()) {
            log.error("Course with ID({}) no found", id);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to remove course failed: course with ID(" + id
                            + ") could not be found. Please, "
                            + "double-check and resubmit.");
            return "redirect:/timetable/management/university/courses";
        }
        Course course = optionalCourse.get();

        timetableFacade.deleteCourse(course);

        redirectAttributes.addFlashAttribute("successAlert",
                "Course ID (" + id + ") was deleted");
        redirectAttributes.addFlashAttribute("editedId", id);

        return "redirect:/timetable/management/university/courses";
    }

    @PostMapping("/new")
    public String addNewCourse(RedirectAttributes redirectAttributes,
            @ModelAttribute("newItemForm") @Valid NewItemForm newItemForm) {

        Course course = new Course(newItemForm.getName());
        course = timetableFacade.saveCourse(course);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Course %s with ID (%d) added to the list",
                        course.getName(), course.getId()));
        redirectAttributes.addFlashAttribute("editedId", course.getId());


        return "redirect:/timetable/management/university/courses";
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

        return "redirect:/timetable/management/university/courses";
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

        return "redirect:/timetable/management/university/courses";
    }

}
