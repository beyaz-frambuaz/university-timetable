package com.foxminded.timetable.controllers;


import com.foxminded.timetable.config.ControllersTestConfig;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.*;
import org.springframework.validation.BindException;

import javax.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    public void postScheduleShouldValidateFormAndRedirectToGroupsWithErrorMessageIfInvalid()
            throws Exception {

        long id = 0L;
        String date = "invalid date";

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date);
        scheduleForm.setId(id);

        RequestBuilder requestBuilder =
                post(baseUrl + "/schedule").flashAttr("scheduleForm",
                        scheduleForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postScheduleShouldRequestGroupFromServiceAndRedirectToGroupsIfNotPresent()
            throws Exception {

        long id = 1L;
        ScheduleForm form = new ScheduleForm();
        form.setId(id);
        form.setDate("2020-06-01");
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

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(filtered);

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

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setFiltered(filtered);

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

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setFiltered(filtered);

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
    public void postRenameShouldValidateFormAndRedirectToGroupsWithErrorMessageIfInvalid()
            throws Exception {

        RenameForm renameForm = new RenameForm();
        renameForm.setNewName(" ");
        renameForm.setRenameId(0L);

        RequestBuilder requestBuilder =
                post(baseUrl + "/rename").flashAttr("renameForm", renameForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postRenameShouldRequestGroupFromServiceAndRedirectToGroupsIfNotPresent()
            throws Exception {

        long id = 1L;
        RenameForm form = new RenameForm();
        form.setRenameId(id);
        form.setNewName("test");
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

        RenameForm form = new RenameForm();
        form.setRenameId(id);
        form.setNewName(name);

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
    public void getRemoveShouldValidateIdAndRedirectToGroupsIfInvalid()
            throws Exception {

        long id = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(ConstraintViolationException.class);
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
    public void postNewShouldValidateFormAndRedirectToGroupsWithErrorMessageIfInvalid()
            throws Exception {

        NewItemForm newItemForm = new NewItemForm();
        newItemForm.setName(" ");

        RequestBuilder requestBuilder =
                post(baseUrl + "/new").flashAttr("newItemForm", newItemForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
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

        NewItemForm form = new NewItemForm();
        form.setName(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newItemForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().saveGroup(newGroup);
    }


}