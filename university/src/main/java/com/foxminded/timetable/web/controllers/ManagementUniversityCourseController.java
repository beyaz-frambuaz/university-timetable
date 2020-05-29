package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.DaySchedule;
import com.foxminded.timetable.model.MonthSchedule;
import com.foxminded.timetable.model.WeekSchedule;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateCourseId;
import com.foxminded.timetable.web.formatter.ScheduleFormatter;
import com.foxminded.timetable.web.forms.NewItemForm;
import com.foxminded.timetable.web.forms.RenameForm;
import com.foxminded.timetable.web.forms.ScheduleForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("**/university/courses")
public class ManagementUniversityCourseController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableService  timetableService;

    @GetMapping("/")
    public String courses(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Course> courses = timetableService.getCourses();
        model.addAttribute("courses", courses);

        RenameForm renameForm = new RenameForm();
        model.addAttribute("renameForm", renameForm);

        NewItemForm newItemForm = new NewItemForm();
        model.addAttribute("newItemForm", newItemForm);

        return "management/university/courses/courses";
    }

    @PostMapping("/schedule")
    public String courseSchedule(Model model,
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Course course;
        try {
            course = timetableService.getCourse(scheduleForm.getId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/courses";
        }
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
                        new SchedulePredicateCourseId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/courses/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateCourseId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/courses/schedule/month";
        }

        return "error/404";
    }

    @GetMapping("/schedule")
    public String redirectCourses() {

        return "redirect:/timetable/management/university/courses";
    }

    @PostMapping("/rename")
    public String rename(RedirectAttributes redirectAttributes,
            @ModelAttribute("renameForm") RenameForm renameForm) {

        Course course;
        try {
            course = timetableService.getCourse(renameForm.getRenameId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/courses";
        }

        course.setName(renameForm.getNewName());
        timetableService.saveCourse(course);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Course ID (%d) is now called %s", course.getId(),
                        course.getName()));
        redirectAttributes.addFlashAttribute("editedId", course.getId());

        return "redirect:/timetable/management/university/courses";
    }

    @PostMapping("/new")
    public String addNewCourse(RedirectAttributes redirectAttributes,
            @ModelAttribute("newItemForm") NewItemForm newItemForm) {

        Course course = new Course(newItemForm.getName());
        course = timetableService.saveCourse(course);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Course %s with ID (%d) added to the list",
                        course.getName(), course.getId()));
        redirectAttributes.addFlashAttribute("editedId", course.getId());


        return "redirect:/timetable/management/university/courses";
    }

}
