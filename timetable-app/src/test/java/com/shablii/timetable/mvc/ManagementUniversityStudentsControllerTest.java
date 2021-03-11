package com.shablii.timetable.mvc;

import com.shablii.timetable.config.ControllersTestConfig;
import com.shablii.timetable.forms.*;
import com.shablii.timetable.forms.utility.*;
import com.shablii.timetable.forms.utility.formatter.ScheduleFormatter;
import com.shablii.timetable.model.*;
import com.shablii.timetable.service.TimetableFacade;
import com.shablii.timetable.service.utility.predicates.*;
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
    public void getStudentsShouldRequestFromServiceAndDisplay() throws Exception {

        List<Student> students = Collections.emptyList();
        given(timetableFacade.getStudents()).willReturn(students);

        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(model().attribute("students", students))
                .andExpect(model().attributeExists("errorAlert", "successAlert", "editedId", "newStudentForm",
                        "changeGroupForm"))
                .andExpect(view().name(baseView + "/students"));

        then(timetableFacade).should().getStudents();
    }

    @Test
    public void postScheduleShouldValidateFormAndRedirectToStudentsWithErrorMessageIfInvalid() throws Exception {

        long id = 0L;
        String date = "invalid date";

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date);
        scheduleForm.setId(id);

        RequestBuilder requestBuilder = post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postScheduleShouldRequestStudentsGroupFromServiceAndRedirectToStudentsIfNotPresent() throws Exception {

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
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest() throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.of(group));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateGroupId(id);

        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(daySchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(model().attribute("group", group))
                .andExpect(view().name(baseView + "/schedule/day"));

        then(scheduleFormatter).should().prepareDaySchedule(predicate, date, filtered);
        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest() throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.of(group));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateGroupId(id);

        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(model().attribute("group", group))
                .andExpect(view().name(baseView + "/schedule/week"));

        then(scheduleFormatter).should().prepareWeekSchedule(predicate, date, filtered);
        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest() throws Exception {

        long id = 1L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.of(group));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateGroupId(id);

        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(model().attribute("group", group))
                .andExpect(view().name(baseView + "/schedule/month"));

        then(scheduleFormatter).should().prepareMonthSchedule(predicate, date, filtered);
        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void getScheduleShouldRedirectToStudents() throws Exception {

        mvc.perform(get(baseUrl + "/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(baseUrl));
    }

    @Test
    public void postNewShouldValidateFormAndRedirectToStudentsWithErrorMessageIfInvalid() throws Exception {

        NewStudentForm newStudentForm = new NewStudentForm();
        newStudentForm.setFirstName(" ");
        newStudentForm.setLastName(" ");
        newStudentForm.setGroupId(0L);

        RequestBuilder requestBuilder = post(baseUrl + "/new").flashAttr("newStudentForm", newStudentForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postNewShouldRequestGroupFromServiceAndRedirectToStudentsIfNotPresent() throws Exception {

        long id = 1L;
        NewStudentForm form = new NewStudentForm();
        form.setGroupId(id);
        form.setFirstName("test");
        form.setLastName("test");
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/new").flashAttr("newStudentForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(id);
    }

    @Test
    public void postNewShouldCreateStudentRequestServiceToSaveAndRedirectToStudentsWithMessage() throws Exception {

        Group group = mock(Group.class);
        long groupId = 2L;
        given(group.getId()).willReturn(groupId);
        given(group.getName()).willReturn("");
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.of(group));

        Student savedStudent = mock(Student.class);
        long studentId = 1L;
        String name = "name";
        given(savedStudent.getId()).willReturn(studentId);
        given(savedStudent.getFullName()).willReturn(name);
        given(savedStudent.getGroup()).willReturn(group);
        given(timetableFacade.saveStudent(any(Student.class))).willReturn(savedStudent);

        Student newStudent = new Student(name, name, group);

        NewStudentForm form = new NewStudentForm();
        form.setGroupId(groupId);
        form.setFirstName(name);
        form.setLastName(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newStudentForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", studentId))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getGroup(groupId);
        then(timetableFacade).should().saveStudent(newStudent);
    }

    @Test
    public void postChangeGroupShouldValidateFormAndRedirectToStudentsWithErrorMessageIfInvalid() throws Exception {

        ChangeGroupForm changeGroupForm = new ChangeGroupForm();
        changeGroupForm.setStudentId(0L);
        changeGroupForm.setNewGroupId(0L);

        RequestBuilder requestBuilder = post(baseUrl + "/change/group").flashAttr("changeGroupForm", changeGroupForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postChangeGroupShouldRequestStudentFromServiceAndRedirectToStudentsIfNotPresent() throws Exception {

        long studentId = 1L;
        ChangeGroupForm form = new ChangeGroupForm();
        form.setStudentId(studentId);
        form.setNewGroupId(studentId);
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/change/group").flashAttr("changeGroupForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(studentId);
    }

    @Test
    public void postChangeGroupShouldRequestGroupFromServiceAndRedirectToStudentsIfNotPresent() throws Exception {

        long studentId = 1L;
        long groupId = 2L;
        ChangeGroupForm form = new ChangeGroupForm();
        form.setStudentId(studentId);
        form.setNewGroupId(groupId);

        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.of(student));
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/change/group").flashAttr("changeGroupForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(studentId);
        then(timetableFacade).should().getGroup(groupId);
    }

    @Test
    public void getRemoveShouldValidateIdAndRedirectToStudentsIfInvalid() throws Exception {

        long id = 0L;

        MvcResult mvcResult = mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void getRemoveShouldRequestStudentFromServiceAndRedirectToStudentsIfNotPresent() throws Exception {

        long id = 1L;
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.empty());

        mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(id);
    }

    @Test
    public void getRemoveShouldRequestServiceToDeleteAndRedirectToStudentsWithMessage() throws Exception {

        long id = 1L;
        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.of(student));

        mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
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

        long studentId = 1L;
        long groupId = 2L;
        ChangeGroupForm form = new ChangeGroupForm();
        form.setStudentId(studentId);
        form.setNewGroupId(groupId);

        Group group = mock(Group.class);
        given(timetableFacade.getGroup(anyLong())).willReturn(Optional.of(group));
        given(group.getName()).willReturn("");

        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.of(student));
        given(student.getId()).willReturn(studentId);
        given(student.getGroup()).willReturn(group);
        given(student.getFirstName()).willReturn("");

        mvc.perform(post(baseUrl + "/change/group").flashAttr("changeGroupForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert", "editedId"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getStudent(studentId);
        then(timetableFacade).should().getGroup(groupId);
        then(student).should().setGroup(group);
        then(timetableFacade).should().saveStudent(student);
    }

}