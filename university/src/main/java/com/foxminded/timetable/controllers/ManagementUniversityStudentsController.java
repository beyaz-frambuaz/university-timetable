package com.foxminded.timetable.controllers;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateGroupId;
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
@RequestMapping("**/university/students")
@Validated
@RequiredArgsConstructor
@Slf4j
public class ManagementUniversityStudentsController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableFacade timetableFacade;

    @GetMapping("/")
    public String students(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        List<Student> students = timetableFacade.getStudents();
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
            @ModelAttribute @Valid ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Optional<Group> optionalGroup =
                timetableFacade.getGroup(scheduleForm.getId());
        if (!optionalGroup.isPresent()) {
            log.error("Group with ID({}) not found", scheduleForm.getId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to locate schedule failed: group with ID("
                            + scheduleForm.getId() + ") could not be found. "
                            + "Please, resubmit the form.");
            return "redirect:/timetable/management/university/students";
        }
        Group group = optionalGroup.get();
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
                                new SchedulePredicateGroupId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/students/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateGroupId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/students/schedule/month";

            default:
                log.error("Faulty schedule form submitted: {}", scheduleForm);
                redirectAttributes.addFlashAttribute("errorAlert", "Attempt to "
                        + "locate schedule failed. We've logged the error and"
                        + " will resolve it ASAP. In the meantime, please "
                        + "try to resubmit the form.");
                return "redirect:/timetable/management/university/students";
        }
    }

    @GetMapping("/schedule")
    public String redirectStudents() {

        return "redirect:/timetable/management/university/students";
    }

    @PostMapping("/change/group")
    public String changeGroup(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid ChangeGroupForm changeGroupForm) {

        Optional<Student> optionalStudent =
                timetableFacade.getStudent(changeGroupForm.getStudentId());
        if (!optionalStudent.isPresent()) {
            log.error("Student with ID({}) not found",
                    changeGroupForm.getStudentId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to change group failed: student with ID("
                            + changeGroupForm.getStudentId() + ") could not "
                            + "be found. Please, resubmit the form.");
            return "redirect:/timetable/management/university/students";
        }
        Student student = optionalStudent.get();

        Optional<Group> optionalGroup =
                timetableFacade.getGroup(changeGroupForm.getNewGroupId());
        if (!optionalGroup.isPresent()) {
            log.error("Group with ID({}) not found",
                    changeGroupForm.getNewGroupId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to change group failed: group with ID("
                            + changeGroupForm.getNewGroupId()
                            + ") could not be found. "
                            + "Please, resubmit the form.");
            return "redirect:/timetable/management/university/students";
        }
        Group group = optionalGroup.get();

        student.setGroup(group);
        timetableFacade.saveStudent(student);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Student %s is now in group %s",
                        student.getFullName(), student.getGroup().getName()));
        redirectAttributes.addFlashAttribute("editedId", student.getId());

        return "redirect:/timetable/management/university/students";
    }

    @GetMapping("/remove")
    public String removeStudent(RedirectAttributes redirectAttributes,
            @RequestParam("id") @IdValid("Student") long id) {

        Optional<Student> optionalStudent = timetableFacade.getStudent(id);
        if (!optionalStudent.isPresent()) {
            log.error("Student with ID({}) no found", id);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to remove student failed: student with ID(" + id
                            + ") could not be found. Please, "
                            + "double-check and resubmit.");
            return "redirect:/timetable/management/university/students";
        }
        Student student = optionalStudent.get();

        timetableFacade.deleteStudent(student);

        redirectAttributes.addFlashAttribute("successAlert",
                "Student ID (" + id + ") was deleted");
        redirectAttributes.addFlashAttribute("editedId", id);

        return "redirect:/timetable/management/university/students";
    }

    @PostMapping("/new")
    public String addNewStudent(RedirectAttributes redirectAttributes,
            @ModelAttribute @Valid NewStudentForm newStudentForm) {

        Optional<Group> optionalGroup =
                timetableFacade.getGroup(newStudentForm.getGroupId());
        if (!optionalGroup.isPresent()) {
            log.error("Group with ID({}) not found",
                    newStudentForm.getGroupId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to add new student failed: group with ID("
                            + newStudentForm.getGroupId() + ") could not be "
                            + "found. Please, resubmit the form.");
            return "redirect:/timetable/management/university/students";
        }
        Group group = optionalGroup.get();

        Student newStudent = new Student(newStudentForm.getFirstName(),
                newStudentForm.getLastName(), group);
        newStudent = timetableFacade.saveStudent(newStudent);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Student %s with ID (%d) is now in group %s",
                        newStudent.getFullName(), newStudent.getId(),
                        newStudent.getGroup().getName()));
        redirectAttributes.addFlashAttribute("editedId", newStudent.getId());

        return "redirect:/timetable/management/university/students";
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

        return "redirect:/timetable/management/university/students";
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

        return "redirect:/timetable/management/university/students";
    }

}
