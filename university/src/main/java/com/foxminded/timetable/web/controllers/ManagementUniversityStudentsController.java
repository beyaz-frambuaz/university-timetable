package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateGroupId;
import com.foxminded.timetable.web.formatter.ScheduleFormatter;
import com.foxminded.timetable.web.forms.ChangeGroupForm;
import com.foxminded.timetable.web.forms.NewStudentForm;
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
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("**/university/students")
public class ManagementUniversityStudentsController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableService  timetableService;

    @GetMapping("/")
    public String students(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Student> students = timetableService.getStudents();
        model.addAttribute("students", students);

        List<Group> groups = students.stream()
                .map(Student::getGroup)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        ChangeGroupForm changeGroupForm = new ChangeGroupForm();
        model.addAttribute("changeGroupForm", changeGroupForm);
        changeGroupForm.setGroups(groups);

        NewStudentForm newStudentForm = new NewStudentForm();
        model.addAttribute("newStudentForm", newStudentForm);
        newStudentForm.setGroups(groups);

        return "management/university/students/students";
    }

    @PostMapping("/schedule")
    public String studentSchedule(Model model,
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Group group;
        try {
            group = timetableService.getGroup(scheduleForm.getId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/students";
        }
        model.addAttribute("group", group);
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "management/university/students/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/students/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/students/schedule/month";
        }

        return "error/404";
    }

    @GetMapping("/schedule")
    public String redirectStudents() {

        return "redirect:/timetable/management/university/students";
    }

    @PostMapping("/change/group")
    public String changeGroup(RedirectAttributes redirectAttributes,
            @ModelAttribute(
                    "changeGroupForm") ChangeGroupForm changeGroupForm) {

        Student student;
        try {
            student = timetableService.getStudent(
                    changeGroupForm.getStudentId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/students";
        }

        Group group;
        try {
            group = timetableService.getGroup(
                    changeGroupForm.getNewGroupId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/students";
        }

        student.setGroup(group);
        timetableService.saveStudent(student);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Student %s is now in group %s",
                        student.getFullName(), student.getGroup().getName()));
        redirectAttributes.addFlashAttribute("editedId", student.getId());

        return "redirect:/timetable/management/university/students";
    }

    @PostMapping("/new")
    public String addNewStudent(RedirectAttributes redirectAttributes,
            @ModelAttribute("newStudentForm") NewStudentForm newStudentForm) {

        Group group;
        try {
             group = timetableService.getGroup(newStudentForm.getGroupId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/students";
        }

        Student newStudent = new Student(newStudentForm.getFirstName(),
                newStudentForm.getLastName(), group);
        newStudent = timetableService.saveStudent(newStudent);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Student %s with ID (%d) is now in group %s",
                        newStudent.getFullName(), newStudent.getId(),
                        newStudent.getGroup().getName()));
        redirectAttributes.addFlashAttribute("editedId", newStudent.getId());


        return "redirect:/timetable/management/university/students";
    }

}
