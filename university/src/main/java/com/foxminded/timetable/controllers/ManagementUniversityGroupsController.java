package com.foxminded.timetable.controllers;

import com.foxminded.timetable.forms.NewItemForm;
import com.foxminded.timetable.forms.RenameForm;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateGroupId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("**/university/groups")
public class ManagementUniversityGroupsController {

    private final ScheduleFormatter scheduleFormatter;
    private final TimetableFacade timetableFacade;

    @GetMapping("/")
    public String groups(Model model,
            @ModelAttribute("successAlert") String successAlert,
            @ModelAttribute("editedId") String editedId,
            @ModelAttribute("errorAlert") String errorAlert) {

        model.addAttribute("errorAlert", errorAlert);
        model.addAttribute("successAlert", successAlert);
        model.addAttribute("editedId", editedId);

        Map<Group, List<Student>> groupedStudents =
                timetableFacade.getGroupedStudents();
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

        Optional<Group> optionalGroup =
                timetableFacade.getGroup(scheduleForm.getId());
        if (!optionalGroup.isPresent()) {
            log.error("Group with ID({}) no found", scheduleForm.getId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to locate schedule failed: group with ID("
                            + scheduleForm.getId()
                            + ") could not be found. Please, resubmit the "
                            + "form.");
            return "redirect:/timetable/management/university/groups";
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

                return "management/university/groups/schedule/day";

            case WEEK:
                WeekSchedule weekSchedule =
                        scheduleFormatter.prepareWeekSchedule(
                                new SchedulePredicateGroupId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("weekSchedule", weekSchedule);

                return "management/university/groups/schedule/week";

            case MONTH:
                MonthSchedule monthSchedule =
                        scheduleFormatter.prepareMonthSchedule(
                                new SchedulePredicateGroupId(
                                        scheduleForm.getId()), date,
                                scheduleForm.isFiltered());
                model.addAttribute("monthSchedule", monthSchedule);

                return "management/university/groups/schedule/month";

            default:
                log.error("Faulty schedule form submitted: {}", scheduleForm);
                redirectAttributes.addFlashAttribute("errorAlert", "Attempt to "
                        + "locate schedule failed. We've logged the error and"
                        + " will resolve it ASAP. In the meantime, please "
                        + "try to resubmit the form.");
                return "redirect:/timetable/management/university/groups";
        }
    }

    @GetMapping("/schedule")
    public String redirectGroups() {

        return "redirect:/timetable/management/university/groups";
    }

    @PostMapping("/rename")
    public String rename(RedirectAttributes redirectAttributes,
            @ModelAttribute("renameForm") RenameForm renameForm) {

        Optional<Group> optionalGroup =
                timetableFacade.getGroup(renameForm.getRenameId());
        if (!optionalGroup.isPresent()) {
            log.error("Group with ID({}) no found", renameForm.getRenameId());
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to rename group failed: group with ID("
                            + renameForm.getRenameId()
                            + ") could not be found. Please, resubmit the "
                            + "form.");
            return "redirect:/timetable/management/university/groups";
        }
        Group group = optionalGroup.get();

        group.setName(renameForm.getNewName());
        timetableFacade.saveGroup(group);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Group ID (%d) is now called %s", group.getId(),
                        group.getName()));
        redirectAttributes.addFlashAttribute("editedId", group.getId());

        return "redirect:/timetable/management/university/groups";
    }

    @GetMapping("/remove")
    public String removeGroup(RedirectAttributes redirectAttributes,
            @RequestParam("id") long id) {

        Optional<Group> optionalGroup = timetableFacade.getGroup(id);
        if (!optionalGroup.isPresent()) {
            log.error("Group with ID({}) no found", id);
            redirectAttributes.addFlashAttribute("errorAlert",
                    "Attempt to remove group failed: group with ID(" + id
                            + ") could not be found. Please, "
                            + "double-check and resubmit.");
            return "redirect:/timetable/management/university/groups";
        }
        Group group = optionalGroup.get();

        timetableFacade.deleteGroup(group);

        redirectAttributes.addFlashAttribute("successAlert",
                "Group ID (" + id + ") was deleted");
        redirectAttributes.addFlashAttribute("editedId", id);

        return "redirect:/timetable/management/university/groups";
    }

    @PostMapping("/new")
    public String addNewGroup(RedirectAttributes redirectAttributes,
            @ModelAttribute("newItemForm") NewItemForm newItemForm) {

        Group group = new Group(newItemForm.getName());
        group = timetableFacade.saveGroup(group);

        redirectAttributes.addFlashAttribute("successAlert",
                String.format("Group %s with ID (%d) added to the list",
                        group.getName(), group.getId()));
        redirectAttributes.addFlashAttribute("editedId", group.getId());


        return "redirect:/timetable/management/university/groups";
    }

}
