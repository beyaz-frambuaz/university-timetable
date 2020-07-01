package com.foxminded.timetable.controllers;

import com.foxminded.timetable.forms.ChangeGroupForm;
import com.foxminded.timetable.forms.NewStudentForm;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllersTestConfig.class)
@WebMvcTest(ManagementUniversityStudentsController.class)
class ManagementUniversityStudentsControllerTest {

    private final String baseUrl = "/timetable/management/university/students";
    private final String baseView = "management/university/students";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private TimetableFacade timetableFacade;

    @Test
    public void getStudentsShouldRequestFromServiceAndDisplay()
            throws Exception {

        List<Student> students = Collections.emptyList();
        given(timetableFacade.getStudents()).willReturn(students);

        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(model().attribute("students", students))
                .andExpect(model().attributeExists("errorAlert", "successAlert",
                        "editedId", "newStudentForm", "changeGroupForm"))
                .andExpect(view().name(baseView + "/students"));

        then(timetableFacade).should().getStudents();
    }

    @Test
    public void postScheduleShouldRequestStudentsGroupFromServiceAndRedirectToStudentsIfNotPresent()
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
    public void getScheduleShouldRedirectToStudents() throws Exception {

        mvc.perform(get(baseUrl + "/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(baseUrl));
    }

    @Test
    public void postNewShouldRequestGroupFromServiceAndRedirectToStudentsIfNotPresent()
            throws Exception {

        NewStudentForm form = mock(NewStudentForm.class);
        long id = 1L;
        given(form.getGroupId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/new").flashAttr("newStudentForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postNewShouldCreateStudentRequestServiceToSaveAndRedirectToStudentsWithMessage()
            throws Exception {

        Group group = mock(Group.class);
        long groupId = 2L;
        given(group.getId()).willReturn(groupId);
        given(group.getName()).willReturn("");
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));

        Student savedStudent = mock(Student.class);
        long studentId = 1L;
        String name = "name";
        given(savedStudent.getId()).willReturn(studentId);
        given(savedStudent.getFullName()).willReturn(name);
        given(savedStudent.getGroup()).willReturn(group);
        given(timetableFacade.saveStudent(any(Student.class))).willReturn(
                savedStudent);

        Student newStudent = new Student(name, name, group);

        NewStudentForm form = mock(NewStudentForm.class);
        given(form.getFirstName()).willReturn(name);
        given(form.getLastName()).willReturn(name);
        given(form.getGroupId()).willReturn(groupId);

        mvc.perform(post(baseUrl + "/new").flashAttr("newStudentForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", studentId))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(groupId);
        then(timetableFacade).should().saveStudent(newStudent);
    }

    @Test
    public void postChangeGroupShouldRequestStudentFromServiceAndRedirectToStudentsIfNotPresent()
            throws Exception {

        ChangeGroupForm form = mock(ChangeGroupForm.class);
        long studentId = 1L;
        given(form.getStudentId()).willReturn(studentId);
        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(post(baseUrl + "/change/group").flashAttr("changeGroupForm",
                form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(studentId);
    }

    @Test
    public void postChangeGroupShouldRequestGroupFromServiceAndRedirectToStudentsIfNotPresent()
            throws Exception {

        ChangeGroupForm form = mock(ChangeGroupForm.class);
        long studentId = 1L;
        long groupId = 2L;
        given(form.getStudentId()).willReturn(studentId);
        given(form.getNewGroupId()).willReturn(groupId);

        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.of(student));
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/change/group").flashAttr("changeGroupForm",
                form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(studentId);
        then(timetableFacade).should().getGroup(groupId);
    }

    @Test
    public void getRemoveShouldRequestStudentFromServiceAndRedirectToStudentsIfNotPresent()
            throws Exception {

        long id = 1L;
        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(id);
    }

    @Test
    public void getRemoveShouldRequestServiceToDeleteAndRedirectToStudentsWithMessage()
            throws Exception {

        long id = 1L;
        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.of(student));

        mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(id);
        then(timetableFacade).should().deleteStudent(student);
    }

    @Test
    public void postChangeGroupShouldResetStudentGroupRequestServiceToSaveStudentAndRedirectToStudentsWithMessage()
            throws Exception {

        ChangeGroupForm form = mock(ChangeGroupForm.class);
        long studentId = 1L;
        long groupId = 2L;
        given(form.getStudentId()).willReturn(studentId);
        given(form.getNewGroupId()).willReturn(groupId);

        Group group = mock(Group.class);
        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));
        given(group.getName()).willReturn("");

        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.of(student));
        given(student.getId()).willReturn(studentId);
        given(student.getGroup()).willReturn(group);
        given(student.getFirstName()).willReturn("");

        mvc.perform(post(baseUrl + "/change/group").flashAttr("changeGroupForm",
                form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert", "editedId"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(studentId);
        then(timetableFacade).should().getGroup(groupId);
        then(student).should().setGroup(group);
        then(timetableFacade).should().saveStudent(student);
    }

}