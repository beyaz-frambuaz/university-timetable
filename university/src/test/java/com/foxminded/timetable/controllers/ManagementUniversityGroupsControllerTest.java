package com.foxminded.timetable.controllers;


import com.foxminded.timetable.forms.NewItemForm;
import com.foxminded.timetable.forms.RenameForm;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.ScheduleOption;
import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateGroupId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllersTestConfig.class)
@WebMvcTest(ManagementUniversityGroupsController.class)
class ManagementUniversityGroupsControllerTest {

    private final String baseUrl = "/timetable/management/university/groups";
    private final String baseView = "management/university/groups";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private TimetableFacade timetableFacade;

    @Test
    public void getGroupsShouldRequestStudentsFromServiceAndDisplayInGroups()
            throws Exception {

        Map<Group, List<Student>> groupedStudents = Collections.emptyMap();
        given(timetableFacade.getGroupedStudents()).willReturn(groupedStudents);

        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("groupedStudents", groupedStudents))
                .andExpect(model().attributeExists("errorAlert", "successAlert",
                        "editedId", "renameForm", "newItemForm"))
                .andExpect(view().name(baseView + "/groups"));

        then(timetableFacade).should().getGroupedStudents();
    }

    @Test
    public void postScheduleShouldRequestGroupFromServiceAndRedirectToGroupsIfNotPresent()
            throws Exception {

        ScheduleForm form = mock(ScheduleForm.class);
        long id = 1L;
        given(form.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest()
            throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        boolean filtered = true;
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.DAY);
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
        SchedulePredicate predicate = new SchedulePredicateGroupId(id);

        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class),
                any(LocalDate.class), anyBoolean())).willReturn(daySchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(model().attribute("group", group))
                .andExpect(view().name(baseView + "/schedule/day"));

        then(scheduleFormatter).should()
                .prepareDaySchedule(predicate, date, filtered);
        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest()
            throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        boolean filtered = true;
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.WEEK);
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
        SchedulePredicate predicate = new SchedulePredicateGroupId(id);

        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(model().attribute("group", group))
                .andExpect(view().name(baseView + "/schedule/week"));

        then(scheduleFormatter).should()
                .prepareWeekSchedule(predicate, date, filtered);
        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest()
            throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        boolean filtered = true;
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getScheduleOption()).willReturn(
                ScheduleOption.MONTH);
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
        SchedulePredicate predicate = new SchedulePredicateGroupId(id);

        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(model().attribute("group", group))
                .andExpect(view().name(baseView + "/schedule/month"));

        then(scheduleFormatter).should()
                .prepareMonthSchedule(predicate, date, filtered);
        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void getScheduleShouldRedirectToGroups() throws Exception {

        mvc.perform(get(baseUrl + "/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(baseUrl));
    }

    @Test
    public void postRenameShouldRequestGroupFromServiceAndRedirectToGroupsIfNotPresent()
            throws Exception {

        RenameForm form = mock(RenameForm.class);
        long id = 1L;
        given(form.getRenameId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/rename").flashAttr("renameForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postRenameShouldSetGroupNameRequestServiceToSaveAndRedirectToGroupsWithMessage()
            throws Exception {

        Group group = mock(Group.class);
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));
        long id = 1L;
        String name = "name";
        given(group.getId()).willReturn(id);
        given(group.getName()).willReturn(name);

        RenameForm form = mock(RenameForm.class);
        given(form.getRenameId()).willReturn(id);
        given(form.getNewName()).willReturn(name);

        mvc.perform(post(baseUrl + "/rename").flashAttr("renameForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
        then(group).should().setName(name);
        then(timetableFacade).should().saveGroup(group);
    }

    @Test
    public void getRemoveShouldRequestGroupFromServiceAndRedirectToGroupsIfNotPresent()
            throws Exception {

        long id = 1L;
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void getRemoveShouldRequestServiceToDeleteAndRedirectToGroupsWithMessage()
            throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));

        mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
        then(timetableFacade).should().deleteGroup(group);
    }

    @Test
    public void postNewShouldCreateGroupRequestServiceToSaveAndRedirectToGroupsWithMessage()
            throws Exception {

        Group savedGroup = mock(Group.class);
        long id = 1L;
        String name = "name";
        given(savedGroup.getId()).willReturn(id);
        given(savedGroup.getName()).willReturn(name);
        given(timetableFacade.saveGroup(any(Group.class))).willReturn(
                savedGroup);

        Group newGroup = new Group(name);

        NewItemForm form = mock(NewItemForm.class);
        given(form.getName()).willReturn(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newItemForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().saveGroup(newGroup);
    }


}