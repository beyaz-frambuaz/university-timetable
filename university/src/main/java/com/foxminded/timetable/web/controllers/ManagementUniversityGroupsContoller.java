package com.foxminded.timetable.web.controllers;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableService;
import com.foxminded.timetable.service.exception.ServiceException;
import com.foxminded.timetable.service.filter.SchedulePredicateGroupId;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("**/university/groups")
public class ManagementUniversityGroupsContoller {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableService  timetableService;

    @GetMapping("/")
    public String groups(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        Map<Group, List<Student>> groupedStudents =
                timetableService.getStudents()
                .stream()
                .sorted(Comparator.comparing(Student::getId))
                .collect(Collectors.groupingBy(Student::getGroup,
                        LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
        model.addAttribute("groupedStudents", groupedStudents);

        RenameForm renameForm = new RenameForm();
        model.addAttribute("renameForm", renameForm);

        NewItemForm newItemForm = new NewItemForm();
        model.addAttribute("newItemForm", newItemForm);

        return "management/university/groups/groups";
    }

    @PostMapping("/schedule")
    public String groupSchedule(Model model,
            @ModelAttribute("scheduleForm") ScheduleForm scheduleForm,
            RedirectAttributes redirectAttributes) {

        Group group;
        try {
            group = timetableService.getGroup(scheduleForm.getId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/groups";
        }
        model.addAttribute("group", group);
        LocalDate date = scheduleForm.getLocalDate();

        switch (scheduleForm.getScheduleOption()) {

            case DAY:
                DaySchedule daySchedule = scheduleFormatter.prepareDaySchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("daySchedule", daySchedule);

                return "management/university/groups/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/groups/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                        new SchedulePredicateGroupId(scheduleForm.getId()),
                        date, scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/groups/schedule/month";
        }

        return "error/404";
    }

    @GetMapping("/schedule")
    public String redirectGroups() {

        return "redirect:/timetable/management/university/groups";
    }

    @PostMapping("/rename")
    public String rename(RedirectAttributes redirectAttributes,
            @ModelAttribute("renameForm") RenameForm renameForm) {

        Group group;
        try {
            group = timetableService.getGroup(renameForm.getRenameId());
        } catch (ServiceException e) {
            redirectAttributes.addFlashAttribute("errorAlert", e.getMessage());
            return "redirect:/timetable/management/university/groups";
        }

        group.setName(renameForm.getNewName());
        timetableService.saveGroup(group);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Group ID (%d) is now called %s", group.getId(),
                        group.getName()));
        redirectAttributes.addFlashAttribute("editedId", group.getId());

        return "redirect:/timetable/management/university/groups";
    }

    @PostMapping("/new")
    public String addNewGroup(RedirectAttributes redirectAttributes,
            @ModelAttribute("newItemForm") NewItemForm newItemForm) {

        Group group = new Group(newItemForm.getName());
        group = timetableService.saveGroup(group);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Group %s with ID (%d) added to the list",
                        group.getName(), group.getId()));
        redirectAttributes.addFlashAttribute("editedId", group.getId());


        return "redirect:/timetable/management/university/groups";
    }

}
