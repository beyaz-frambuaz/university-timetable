package com.foxminded.timetable.controllers;

import com.foxminded.timetable.config.ControllersTestConfig;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.*;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.model.generator.DataGenerator;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.*;
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
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagementController.class)
@Import(ControllersTestConfig.class)
class ManagementControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private OptionsFormatter optionsFormatter;
    @MockBean
    private DataGenerator dataGenerator;
    @MockBean
    private TimetableFacade timetableFacade;
    @MockBean
    private SemesterCalendar semesterCalendar;

    @Test
    public void getHomeShouldClearSessionRequestDayScheduleAndReturnHomeViewWithFlashMessages()
            throws Exception {

        DaySchedule daySchedule = mock(DaySchedule.class);
        given(daySchedule.getDayDescription()).willReturn("");
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class),
                any(LocalDate.class), anyBoolean())).willReturn(daySchedule);

        mvc.perform(get("/timetable/management/home"))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("student",
                        "professor"))
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(
                        model().attributeExists("errorAlert", "successAlert"))
                .andExpect(view().name("management/home"));

        then(scheduleFormatter).should()
                .prepareDaySchedule(new SchedulePredicateNoFilter(),
                        LocalDate.now(), false);
    }

    @Test
    public void postRebuildTimetableShouldTriggerDataGeneratorAndRedirectHome()
            throws Exception {

        mvc.perform(post("/timetable/management/rebuild-timetable"))
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/management/home"));

        then(dataGenerator).should().refreshTimetableData();
    }

    @Test
    public void postRefreshAllDataShouldTriggerDataGeneratorAndRedirectHome()
            throws Exception {

        mvc.perform(post("/timetable/management/refresh-all-data"))
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/management/home"));

        then(dataGenerator).should().refreshAllData();
    }

    @ParameterizedTest
    @ValueSource(strings = { "/timetable/management/schedule",
            "/timetable/management/schedule/reschedule",
            "/timetable/management/schedule/options" })
    public void getSchedulePagesShouldRedirectHome(String uri)
            throws Exception {

        mvc.perform(get(uri))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/timetable/management/home"));
    }

    @Test
    public void getTwoWeekShouldRequestFromFormatterAndDisplayTwoWeekSchedule()
            throws Exception {

        TwoWeekSchedule twoWeekSchedule = mock(TwoWeekSchedule.class);
        given(scheduleFormatter.prepareTwoWeekSchedule()).willReturn(
                twoWeekSchedule);

        mvc.perform(get("/timetable/management/two_week"))
                .andExpect(status().isOk())
                .andExpect(
                        model().attribute("twoWeekSchedule", twoWeekSchedule))
                .andExpect(view().name("management/schedule/two_week"));

        then(scheduleFormatter).should().prepareTwoWeekSchedule();
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest()
            throws Exception {

        LocalDate date = LocalDate.MAX;
        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setId(1L);

        SchedulePredicate predicate = new SchedulePredicateNoFilter();
        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class),
                any(LocalDate.class), anyBoolean())).willReturn(daySchedule);

        RequestBuilder requestBuilder =
                post("/timetable/management/schedule").flashAttr("scheduleForm",
                        scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(view().name("management/schedule/day"));

        then(scheduleFormatter).should()
                .prepareDaySchedule(predicate, date, false);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest()
            throws Exception {

        ScheduleForm scheduleForm = new ScheduleForm();
        LocalDate date = LocalDate.MAX;
        scheduleForm.setDate(date.toString());
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setId(1L);

        SchedulePredicate predicate = new SchedulePredicateNoFilter();
        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        RequestBuilder requestBuilder =
                post("/timetable/management/schedule").flashAttr("scheduleForm",
                        scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(view().name("management/schedule/week"));

        then(scheduleFormatter).should()
                .prepareWeekSchedule(predicate, date, false);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest()
            throws Exception {

        LocalDate date = LocalDate.MAX;
        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setId(1L);

        SchedulePredicate predicate = new SchedulePredicateNoFilter();
        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        RequestBuilder requestBuilder =
                post("/timetable/management/schedule").flashAttr("scheduleForm",
                        scheduleForm);
        mvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(view().name("management/schedule/month"));

        then(scheduleFormatter).should()
                .prepareMonthSchedule(predicate, date, false);
    }

    @Test
    public void postScheduleShouldValidateFormRedirectToListWithErrorMessageIfInvalid()
            throws Exception {

        long id = 0L;
        String date = "invalid date";

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date);
        scheduleForm.setId(id);

        RequestBuilder requestBuilder =
                post("/timetable/management/schedule").flashAttr("scheduleForm",
                        scheduleForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void getAvailableShouldRequestFromServiceAndDisplayAvailableProfessorsAndAuditoriums()
            throws Exception {

        long scheduleId = 1L;
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        Schedule schedule = mock(Schedule.class);
        Auditorium auditorium = mock(Auditorium.class);
        Course course = mock(Course.class);
        Group group = mock(Group.class);
        Professor professor = mock(Professor.class);

        given(schedule.getDate()).willReturn(date);
        given(schedule.getPeriod()).willReturn(period);
        given(schedule.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn("");
        given(schedule.getCourse()).willReturn(course);
        given(course.getName()).willReturn("");
        given(schedule.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");
        given(schedule.getProfessor()).willReturn(professor);
        given(professor.getFullName()).willReturn("");

        List<Professor> professors = Collections.emptyList();
        List<Auditorium> auditoriums = Collections.emptyList();
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.getAvailableProfessors(any(LocalDate.class),
                any(Period.class))).willReturn(professors);
        given(timetableFacade.getAvailableAuditoriums(any(LocalDate.class),
                any(Period.class))).willReturn(auditoriums);

        mvc.perform(get("/timetable/management/schedule/available").queryParam(
                "scheduleId", String.valueOf(scheduleId)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("successAlert", "errorAlert",
                        "changeScheduleForm"))
                .andExpect(model().attribute("schedule", schedule))
                .andExpect(model().attribute("professors", professors))
                .andExpect(model().attribute("auditoriums", auditoriums))
                .andExpect(view().name("management/schedule/available"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getAvailableProfessors(date, period);
        then(timetableFacade).should().getAvailableAuditoriums(date, period);
    }

    @Test
    public void getAvailableShouldRequestScheduleAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get("/timetable/management/schedule/available").queryParam(
                        "scheduleId", String.valueOf(scheduleId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());
        assertThat(exception).isPresent()
                .containsInstanceOf(NotFoundException.class);

        then(timetableFacade).should().getSchedule(scheduleId);
    }

    @Test
    public void getAvailableShouldValidateScheduleIdAndRedirectToHomeWithErrorMessageIfInvalid()
            throws Exception {

        long scheduleId = 0L;

        MvcResult mvcResult = mvc.perform(
                get("/timetable/management/schedule/available").queryParam(
                        "scheduleId", String.valueOf(scheduleId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void postAvailableShouldRequestScheduleAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(scheduleId);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/available").flashAttr(
                        "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());
        assertThat(exception).isPresent()
                .containsInstanceOf(NotFoundException.class);

        then(timetableFacade).should().getSchedule(scheduleId);
    }

    @Test
    public void postAvailableShouldValidateFormAndRedirectToHomeWithErrorMessageIfInvalid()
            throws Exception {

        long id = 0L;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(id);
        form.setProfessorId(id);
        form.setAuditoriumId(id);

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/available").flashAttr(
                        "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postAvailableShouldRequestNewAuditoriumAndRedirectToAvailableIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        long auditoriumId = 2L;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(scheduleId);
        form.setAuditoriumId(auditoriumId);

        Schedule schedule = mock(Schedule.class);
        given(schedule.getId()).willReturn(scheduleId);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.getAuditorium(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(post("/timetable/management/schedule/available").flashAttr(
                "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/schedule"
                        + "/available?scheduleId=" + scheduleId));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getAuditorium(auditoriumId);
    }

    @Test
    public void postAvailableShouldResetScheduleAuditoriumRequestToSaveScheduleAndRedirectToAvailable()
            throws Exception {

        long scheduleId = 1L;
        long auditoriumId = 2L;
        Long professorId = null;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(scheduleId);
        form.setAuditoriumId(auditoriumId);
        form.setProfessorId(professorId);

        Schedule schedule = mock(Schedule.class);
        given(schedule.getId()).willReturn(scheduleId);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        Auditorium auditorium = mock(Auditorium.class);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(
                Optional.of(auditorium));

        mvc.perform(post("/timetable/management/schedule/available").flashAttr(
                "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(redirectedUrl("/timetable/management/schedule"
                        + "/available?scheduleId=" + scheduleId));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getAuditorium(auditoriumId);
        then(schedule).should().setAuditorium(auditorium);
        then(timetableFacade).should().saveSchedule(schedule);
    }

    @Test
    public void postAvailableShouldRequestNewProfessorAndRedirectToAvailableIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        Long auditoriumId = null;
        long professorId = 3L;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(scheduleId);
        form.setAuditoriumId(auditoriumId);
        form.setProfessorId(professorId);

        Schedule schedule = mock(Schedule.class);
        given(schedule.getId()).willReturn(scheduleId);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(post("/timetable/management/schedule/available").flashAttr(
                "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/schedule"
                        + "/available?scheduleId=" + scheduleId));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getProfessor(professorId);
    }

    @Test
    public void postAvailableShouldResetScheduleProfessorRequestToSaveScheduleAndRedirectToAvailable()
            throws Exception {

        long scheduleId = 1L;
        Long auditoriumId = null;
        long professorId = 3L;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(scheduleId);
        form.setAuditoriumId(auditoriumId);
        form.setProfessorId(professorId);

        Schedule schedule = mock(Schedule.class);
        given(schedule.getId()).willReturn(scheduleId);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        Professor professor = mock(Professor.class);
        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));

        mvc.perform(post("/timetable/management/schedule/available").flashAttr(
                "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(redirectedUrl("/timetable/management/schedule"
                        + "/available?scheduleId=" + scheduleId));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getProfessor(professorId);
        then(schedule).should().setProfessor(professor);
        then(timetableFacade).should().saveSchedule(schedule);
    }

    @Test
    public void postAvailableShouldResetScheduleProfessorAndAuditoriumRequestToSaveScheduleAndRedirectToAvailable()
            throws Exception {

        long scheduleId = 1L;
        long auditoriumId = 2L;
        long professorId = 3L;
        ChangeScheduleForm form = new ChangeScheduleForm();
        form.setScheduleId(scheduleId);
        form.setAuditoriumId(auditoriumId);
        form.setProfessorId(professorId);

        Schedule schedule = mock(Schedule.class);
        given(schedule.getId()).willReturn(scheduleId);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        Auditorium auditorium = mock(Auditorium.class);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(
                Optional.of(auditorium));

        Professor professor = mock(Professor.class);
        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));

        mvc.perform(post("/timetable/management/schedule/available").flashAttr(
                "changeScheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(redirectedUrl("/timetable/management/schedule"
                        + "/available?scheduleId=" + scheduleId));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getAuditorium(auditoriumId);
        then(timetableFacade).should().getProfessor(professorId);
        then(schedule).should().setAuditorium(auditorium);
        then(schedule).should().setProfessor(professor);
        then(timetableFacade).should().saveSchedule(schedule);
    }

    @Test
    public void postOptionsShouldRequestScheduleAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        FindReschedulingOptionsForm form = new FindReschedulingOptionsForm();
        form.setScheduleId(scheduleId);
        form.setDate("2020-06-01");
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/options").flashAttr(
                        "findReschedulingOptionsForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());
        assertThat(exception).isPresent()
                .containsInstanceOf(NotFoundException.class);

        then(timetableFacade).should().getSchedule(scheduleId);
    }

    @Test
    public void postOptionsShouldValidateFormAndRedirectToHomeWithErrorMessageIfInvalid()
            throws Exception {

        long scheduleId = 0L;
        String invalidDate = "invalid date";
        FindReschedulingOptionsForm form = new FindReschedulingOptionsForm();
        form.setScheduleId(scheduleId);
        form.setDate(invalidDate);

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/options").flashAttr(
                        "findReschedulingOptionsForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postOptionsShouldRequestAndDisplayDayOptionsFromFormatter()
            throws Exception {

        long scheduleId = 1L;
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        Schedule schedule = mock(Schedule.class);
        Auditorium auditorium = mock(Auditorium.class);
        Course course = mock(Course.class);
        Group group = mock(Group.class);
        Professor professor = mock(Professor.class);

        given(schedule.getDate()).willReturn(date);
        given(schedule.getPeriod()).willReturn(period);
        given(schedule.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn("");
        given(schedule.getCourse()).willReturn(course);
        given(course.getName()).willReturn("");
        given(schedule.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");
        given(schedule.getProfessor()).willReturn(professor);
        given(professor.getFullName()).willReturn("");

        FindReschedulingOptionsForm form = new FindReschedulingOptionsForm();
        form.setScheduleId(scheduleId);
        form.setScheduleOption(ScheduleOption.DAY);
        form.setDate(date.toString());

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        DayOptions dayOptions = mock(DayOptions.class);
        given(optionsFormatter.prepareDayOptions(any(Schedule.class),
                any(LocalDate.class))).willReturn(dayOptions);

        mvc.perform(post("/timetable/management/schedule/options").flashAttr(
                "findReschedulingOptionsForm", form))
                .andExpect(status().isOk())
                .andExpect(model().attribute("schedule", schedule))
                .andExpect(model().attribute("dayOptions", dayOptions))
                .andExpect(model().attributeExists("rescheduleForm"))
                .andExpect(view().name("management/schedule/options"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(optionsFormatter).should().prepareDayOptions(schedule, date);
    }

    @Test
    public void postOptionsShouldRequestAndDisplayWeekOptionsFromFormatter()
            throws Exception {

        long scheduleId = 1L;
        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        Schedule schedule = mock(Schedule.class);
        Auditorium auditorium = mock(Auditorium.class);
        Course course = mock(Course.class);
        Group group = mock(Group.class);
        Professor professor = mock(Professor.class);

        given(schedule.getDate()).willReturn(date);
        given(schedule.getPeriod()).willReturn(period);
        given(schedule.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn("");
        given(schedule.getCourse()).willReturn(course);
        given(course.getName()).willReturn("");
        given(schedule.getGroup()).willReturn(group);
        given(group.getName()).willReturn("");
        given(schedule.getProfessor()).willReturn(professor);
        given(professor.getFullName()).willReturn("");

        FindReschedulingOptionsForm form = new FindReschedulingOptionsForm();
        form.setScheduleId(scheduleId);
        form.setScheduleOption(ScheduleOption.WEEK);
        form.setDate(date.toString());

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        WeekOptions weekOptions = mock(WeekOptions.class);
        given(optionsFormatter.prepareWeekOptions(any(Schedule.class),
                anyInt())).willReturn(weekOptions);

        given(semesterCalendar.getSemesterWeekNumber(
                any(LocalDate.class))).willReturn(1);

        mvc.perform(post("/timetable/management/schedule/options").flashAttr(
                "findReschedulingOptionsForm", form))
                .andExpect(status().isOk())
                .andExpect(model().attribute("schedule", schedule))
                .andExpect(model().attribute("weekOptions", weekOptions))
                .andExpect(model().attributeExists("rescheduleForm"))
                .andExpect(view().name("management/schedule/options"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(optionsFormatter).should().prepareWeekOptions(schedule, 1);
    }

    @Test
    public void postRescheduleShouldValidateFormAndRedirectToHomeWithErrorMessageIfInvalid()
            throws Exception {

        long id = 0L;
        String invalidDate = "invalid date";
        RescheduleForm form = new RescheduleForm();
        form.setScheduleId(id);
        form.setOptionId(id);
        form.setDate(invalidDate);

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/reschedule").flashAttr(
                        "rescheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postRescheduleShouldRequestScheduleAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        RescheduleForm form = new RescheduleForm();
        form.setScheduleId(scheduleId);
        form.setOptionId(scheduleId);
        form.setDate("2020-06-01");

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/reschedule").flashAttr(
                        "rescheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());
        assertThat(exception).isPresent()
                .containsInstanceOf(NotFoundException.class);

        then(timetableFacade).should().getSchedule(scheduleId);
    }

    @Test
    public void postRescheduleShouldRequestOptionAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        long optionId = 2L;
        RescheduleForm form = new RescheduleForm();
        form.setScheduleId(scheduleId);
        form.setOptionId(optionId);
        form.setDate("2020-06-01");

        Schedule schedule = mock(Schedule.class);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.getOption(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                post("/timetable/management/schedule/reschedule").flashAttr(
                        "rescheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl("/timetable/management/home"))
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());
        assertThat(exception).isPresent()
                .containsInstanceOf(NotFoundException.class);

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getOption(optionId);
    }

    @Test
    public void postRescheduleShouldRequestServiceToRescheduleOnceAndDisplayAffectedSchedule()
            throws Exception {

        long scheduleId = 1L;
        long optionId = 2L;
        LocalDate date = LocalDate.MAX;
        RescheduleForm form = new RescheduleForm();
        form.setScheduleId(scheduleId);
        form.setOptionId(optionId);
        form.setRescheduleFormOption(RescheduleFormOption.ONCE);
        form.setDate(date.toString());

        Auditorium auditorium = new Auditorium("");
        Course course = new Course("");
        Group group = new Group("");
        Professor professor = new Professor("", "");
        Schedule schedule =
                new Schedule(scheduleId, null, date, DayOfWeek.MONDAY,
                        Period.FIRST, auditorium, course, group, professor);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        ReschedulingOption option = mock(ReschedulingOption.class);
        given(timetableFacade.getOption(anyLong())).willReturn(
                Optional.of(option));

        given(timetableFacade.rescheduleSingle(any(Schedule.class),
                any(LocalDate.class),
                any(ReschedulingOption.class))).willReturn(schedule);
        List<Schedule> affected = Collections.singletonList(schedule);

        mvc.perform(post("/timetable/management/schedule/reschedule").flashAttr(
                "rescheduleForm", form))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("initial", "message"))
                .andExpect(model().attribute("affected", affected))
                .andExpect(view().name("management/schedule/reschedule"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getOption(optionId);
        then(timetableFacade).should().rescheduleSingle(schedule, date, option);
    }

    @Test
    public void postRescheduleShouldRequestServiceToReschedulePermanentlyAndDisplayAffectedSchedules()
            throws Exception {

        long scheduleId = 1L;
        long optionId = 2L;
        LocalDate date = LocalDate.MAX;
        RescheduleForm form = new RescheduleForm();
        form.setScheduleId(scheduleId);
        form.setOptionId(optionId);
        form.setRescheduleFormOption(RescheduleFormOption.PERMANENTLY);
        form.setDate(date.toString());

        Auditorium auditorium = new Auditorium("");
        Course course = new Course("");
        Group group = new Group("");
        Professor professor = new Professor("", "");
        Schedule schedule =
                new Schedule(scheduleId, null, date, DayOfWeek.MONDAY,
                        Period.FIRST, auditorium, course, group, professor);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        ReschedulingOption option = mock(ReschedulingOption.class);
        given(timetableFacade.getOption(anyLong())).willReturn(
                Optional.of(option));

        List<Schedule> affected = Collections.singletonList(schedule);
        given(timetableFacade.rescheduleRecurring(any(Schedule.class),
                any(LocalDate.class),
                any(ReschedulingOption.class))).willReturn(affected);

        mvc.perform(post("/timetable/management/schedule/reschedule").flashAttr(
                "rescheduleForm", form))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("initial", "message"))
                .andExpect(model().attribute("affected", affected))
                .andExpect(view().name("management/schedule/reschedule"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(timetableFacade).should().getOption(optionId);
        then(timetableFacade).should()
                .rescheduleRecurring(schedule, date, option);
    }

}