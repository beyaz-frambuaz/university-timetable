package com.shablii.timetable.mvc;

import com.shablii.timetable.config.ControllersTestConfig;
import com.shablii.timetable.exceptions.SessionExpiredException;
import com.shablii.timetable.forms.*;
import com.shablii.timetable.forms.utility.*;
import com.shablii.timetable.forms.utility.formatter.ScheduleFormatter;
import com.shablii.timetable.model.*;
import com.shablii.timetable.service.TimetableFacade;
import com.shablii.timetable.service.utility.predicates.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentController.class)
@Import(ControllersTestConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;
    @MockBean
    private ScheduleFormatter scheduleFormatter;

    @Test
    public void getListShouldClearSessionAndReturnListView() throws Exception {

        mvc.perform(get("/timetable/students/list"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("student", "professor"))
                .andExpect(view().name("students/list"));
    }

    @Test
    public void getListShouldRequestStudentsFromServiceAndAddToModel() throws Exception {

        List<Student> students = Collections.emptyList();
        given(timetableFacade.getStudents()).willReturn(students);

        mvc.perform(get("/timetable/students/list")).andExpect(model().attribute("students", students));

        then(timetableFacade).should().getStudents();
    }

    @Test
    public void getListShouldAddFlashMassagesToModel() throws Exception {

        mvc.perform(get("/timetable/students/list")).andExpect(model().attributeExists("errorAlert", "sessionExpired"));
    }

    @Test
    public void postListShouldRequestStudentFromServiceCheckPresenceAddToSessionAndRedirectToHome() throws Exception {

        long id = 1L;
        Student student = mock(Student.class);
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.of(student));

        ResultActions resultActions = mvc.perform(
                post("/timetable/students/list").param("studentId", String.valueOf(id)));
        resultActions.andExpect(request().sessionAttribute("student", student))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/students/student/home"));

        then(timetableFacade).should().getStudent(id);
    }

    @Test
    public void postListShouldRedirectToListIfStudentNotPresent() throws Exception {

        long id = 1L;
        given(timetableFacade.getStudent(anyLong())).willReturn(Optional.empty());

        ResultActions resultActions = mvc.perform(
                post("/timetable/students/list").param("studentId", String.valueOf(id)));
        resultActions.andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/students/list"));

        then(timetableFacade).should().getStudent(id);
    }

    @Test
    public void postListShouldValidateIdAndRedirectToListWithErrorMessageIfInvalid() throws Exception {

        long id = 0;
        MvcResult mvcResult = mvc.perform(post("/timetable/students/list").param("studentId", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/students/list"))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void getHomeShouldRequestTodaysScheduleFromFormatterAndReturnHomeView() throws Exception {

        boolean filtered = true;
        long id = 1L;
        SchedulePredicate predicate = new SchedulePredicateGroupId(id);
        Group group = mock(Group.class);
        Student student = mock(Student.class);
        given(student.getGroup()).willReturn(group);
        given(group.getId()).willReturn(id);
        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(daySchedule);

        mvc.perform(get("/timetable/students/student/home").sessionAttr("student", student))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(view().name("students/student/home"));

        then(scheduleFormatter).should().prepareDaySchedule(predicate, LocalDate.now(), filtered);
    }

    @Test
    public void getTwoWeekShouldRequestScheduleFromFormatterAndReturnTwoWeekView() throws Exception {

        Group group = mock(Group.class);
        Student student = mock(Student.class);
        given(student.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");
        TwoWeekSchedule twoWeekSchedule = mock(TwoWeekSchedule.class);
        given(scheduleFormatter.prepareTwoWeekSchedule()).willReturn(twoWeekSchedule);

        mvc.perform(get("/timetable/students/student/two_week").sessionAttr("student", student))
                .andExpect(status().isOk())
                .andExpect(model().attribute("twoWeekSchedule", twoWeekSchedule))
                .andExpect(view().name("students/student/schedule/two_week"));

        then(scheduleFormatter).should().prepareTwoWeekSchedule();
    }

    @Test
    public void getScheduleShouldRedirectToHome() throws Exception {

        mvc.perform(get("/timetable/students/student/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/students/student/home"));
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest() throws Exception {

        boolean filtered = true;
        long id = 1L;
        LocalDate date = LocalDate.of(2020, 6, 1);

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(filtered);

        Group group = mock(Group.class);
        Student student = mock(Student.class);
        given(student.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");

        SchedulePredicate predicate = new SchedulePredicateGroupId(id);
        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(daySchedule);

        RequestBuilder requestBuilder = post("/timetable/students/student/schedule").sessionAttr("student", student)
                .flashAttr("scheduleForm", scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(view().name("students/student/schedule/day"));

        then(scheduleFormatter).should().prepareDaySchedule(predicate, date, filtered);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest() throws Exception {

        boolean filtered = true;
        long id = 1L;
        LocalDate date = LocalDate.of(2020, 6, 1);

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setFiltered(filtered);

        Group group = mock(Group.class);
        Student student = mock(Student.class);
        given(student.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");

        SchedulePredicate predicate = new SchedulePredicateGroupId(id);
        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        RequestBuilder requestBuilder = post("/timetable/students/student/schedule").sessionAttr("student", student)
                .flashAttr("scheduleForm", scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(view().name("students/student/schedule/week"));

        then(scheduleFormatter).should().prepareWeekSchedule(predicate, date, filtered);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest() throws Exception {

        boolean filtered = true;
        long id = 1L;
        LocalDate date = LocalDate.of(2020, 6, 1);

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setFiltered(filtered);

        Group group = mock(Group.class);
        Student student = mock(Student.class);
        given(student.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");

        SchedulePredicate predicate = new SchedulePredicateGroupId(id);
        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        RequestBuilder requestBuilder = post("/timetable/students/student/schedule").sessionAttr("student", student)
                .flashAttr("scheduleForm", scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(view().name("students/student/schedule/month"));

        then(scheduleFormatter).should().prepareMonthSchedule(predicate, date, filtered);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/timetable/students/student/home", "/timetable/students/student/two_week" })
    public void getStudentPagesShouldThrowExceptionIfNoStudentInSession(String uri) throws Exception {

        MvcResult mvcResult = mvc.perform(get(uri))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sessionExpired"))
                .andExpect(redirectedUrl("/timetable/students/list"))
                .andReturn();

        Optional<SessionExpiredException> sessionExpiredException = Optional.ofNullable(
                (SessionExpiredException) mvcResult.getResolvedException());

        assertThat(sessionExpiredException).isPresent().containsInstanceOf(SessionExpiredException.class);
    }

    @Test
    public void postScheduleShouldThrowExceptionIfNoStudentInSession() throws Exception {

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate("2020-01-01");
        scheduleForm.setId(1L);

        MvcResult mvcResult = mvc.perform(
                post("/timetable/students/student/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("sessionExpired"))
                .andExpect(redirectedUrl("/timetable/students/list"))
                .andReturn();

        Optional<SessionExpiredException> sessionExpiredException = Optional.ofNullable(
                (SessionExpiredException) mvcResult.getResolvedException());

        assertThat(sessionExpiredException).isPresent().containsInstanceOf(SessionExpiredException.class);
    }

    @Test
    public void postScheduleShouldValidateFormAndRedirectToListWithErrorMessageIfInvalid() throws Exception {

        long id = 0L;
        String date = "invalid date";

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date);
        scheduleForm.setId(id);

        Student student = mock(Student.class);

        RequestBuilder requestBuilder = post("/timetable/students/student/schedule").sessionAttr("student", student)
                .flashAttr("scheduleForm", scheduleForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/students/list"))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

}