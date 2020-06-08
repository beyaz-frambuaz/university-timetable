package com.foxminded.timetable.controllers;

import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.exceptions.ServiceException;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.OptionsFormatter;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.model.generator.DataGenerator;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateNoFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ManagementController.class)
@Import(ControllersTestConfig.class)
class ManagementControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private OptionsFormatter  optionsFormatter;
    @MockBean
    private DataGenerator     dataGenerator;
    @MockBean
    private TimetableFacade   timetableFacade;
    @MockBean
    private SemesterCalendar  semesterCalendar;

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

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.DAY);
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

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.WEEK);
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

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getScheduleOption()).willReturn(
                ScheduleOption.MONTH);
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
    public void postAvailableShouldRequestScheduleAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        ChangeScheduleForm form = mock(ChangeScheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
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
    public void postAvailableShouldRequestNewAuditoriumAndRedirectToAvailableIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        long auditoriumId = 2L;
        ChangeScheduleForm form = mock(ChangeScheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getAuditoriumId()).willReturn(auditoriumId);

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
        ChangeScheduleForm form = mock(ChangeScheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getAuditoriumId()).willReturn(auditoriumId);
        given(form.getProfessorId()).willReturn(professorId);

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
        ChangeScheduleForm form = mock(ChangeScheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getAuditoriumId()).willReturn(auditoriumId);
        given(form.getProfessorId()).willReturn(professorId);

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
        ChangeScheduleForm form = mock(ChangeScheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getAuditoriumId()).willReturn(auditoriumId);
        given(form.getProfessorId()).willReturn(professorId);

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
        ChangeScheduleForm form = mock(ChangeScheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getAuditoriumId()).willReturn(auditoriumId);
        given(form.getProfessorId()).willReturn(professorId);

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
        FindReschedulingOptionsForm form =
                mock(FindReschedulingOptionsForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
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
    public void postOptionsShouldRequestAndDisplayWeekOptionsFromFormatterForOneDayWhenRequestedInForm()
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

        FindReschedulingOptionsForm form =
                mock(FindReschedulingOptionsForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getScheduleOption()).willReturn(ScheduleOption.DAY);
        given(form.getLocalDate()).willReturn(date);

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        WeekOptions weekOptions = mock(WeekOptions.class);
        given(optionsFormatter.prepareWeekOptions(any(Schedule.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                weekOptions);

        mvc.perform(post("/timetable/management/schedule/options").flashAttr(
                "findReschedulingOptionsForm", form))
                .andExpect(status().isOk())
                .andExpect(model().attribute("schedule", schedule))
                .andExpect(model().attribute("weekOptions", weekOptions))
                .andExpect(model().attributeExists("rescheduleForm"))
                .andExpect(view().name("management/schedule/options"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(optionsFormatter).should()
                .prepareWeekOptions(schedule, date, date);
    }

    @Test
    public void postOptionsShouldRequestAndDisplayWeekOptionsFromFormatterForEntireWeekWhenRequestedInForm()
            throws Exception {

        long scheduleId = 1L;
        LocalDate date = LocalDate.MAX;
        LocalDate monday = LocalDate.of(2020, 1, 1);
        LocalDate friday = LocalDate.of(2020, 12, 12);
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

        FindReschedulingOptionsForm form =
                mock(FindReschedulingOptionsForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getScheduleOption()).willReturn(ScheduleOption.WEEK);
        given(form.getLocalDate()).willReturn(date);

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        WeekOptions weekOptions = mock(WeekOptions.class);
        given(optionsFormatter.prepareWeekOptions(any(Schedule.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                weekOptions);

        given(semesterCalendar.getWeekMonday(any(LocalDate.class))).willReturn(
                monday);
        given(semesterCalendar.getWeekFriday(any(LocalDate.class))).willReturn(
                friday);

        mvc.perform(post("/timetable/management/schedule/options").flashAttr(
                "findReschedulingOptionsForm", form))
                .andExpect(status().isOk())
                .andExpect(model().attribute("schedule", schedule))
                .andExpect(model().attribute("weekOptions", weekOptions))
                .andExpect(model().attributeExists("rescheduleForm"))
                .andExpect(view().name("management/schedule/options"));

        then(timetableFacade).should().getSchedule(scheduleId);
        then(optionsFormatter).should()
                .prepareWeekOptions(schedule, monday, friday);
    }

    @Test
    public void postRescheduleShouldRequestScheduleAndThrowExceptionIfNotPresent()
            throws Exception {

        long scheduleId = 1L;
        RescheduleForm form = mock(RescheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
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
        RescheduleForm form = mock(RescheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getOptionId()).willReturn(optionId);

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
        RescheduleForm form = mock(RescheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getOptionId()).willReturn(optionId);
        given(form.getRescheduleFormOption()).willReturn(
                RescheduleFormOption.ONCE);
        given(form.getLocalDate()).willReturn(date);

        Auditorium auditorium = new Auditorium("");
        Course course = new Course("");
        Group group = new Group("");
        Professor professor = new Professor("", "");
        Schedule schedule =
                new Schedule(scheduleId, scheduleId, date, DayOfWeek.MONDAY,
                        Period.FIRST, auditorium, course, group, professor);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        ReschedulingOption option = mock(ReschedulingOption.class);
        given(timetableFacade.getOption(anyLong())).willReturn(
                Optional.of(option));

        given(timetableFacade.rescheduleOnce(any(Schedule.class),
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
        then(timetableFacade).should().rescheduleOnce(schedule, date, option);
    }

    @Test
    public void postRescheduleShouldRequestServiceToReschedulePermanentlyAndDisplayAffectedSchedules()
            throws Exception {

        long scheduleId = 1L;
        long optionId = 2L;
        LocalDate date = LocalDate.MAX;
        RescheduleForm form = mock(RescheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getOptionId()).willReturn(optionId);
        given(form.getRescheduleFormOption()).willReturn(
                RescheduleFormOption.PERMANENTLY);
        given(form.getLocalDate()).willReturn(date);

        Auditorium auditorium = new Auditorium("");
        Course course = new Course("");
        Group group = new Group("");
        Professor professor = new Professor("", "");
        Schedule schedule =
                new Schedule(scheduleId, scheduleId, date, DayOfWeek.MONDAY,
                        Period.FIRST, auditorium, course, group, professor);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        ReschedulingOption option = mock(ReschedulingOption.class);
        given(timetableFacade.getOption(anyLong())).willReturn(
                Optional.of(option));

        List<Schedule> affected = Collections.singletonList(schedule);
        given(timetableFacade.reschedulePermanently(any(Schedule.class),
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
                .reschedulePermanently(schedule, date, option);
    }

    @Test
    public void postRescheduleShouldRequestServiceToReschedulePermanentlyAndRethrowException()
            throws Exception {

        long scheduleId = 1L;
        long optionId = 2L;
        LocalDate date = LocalDate.MAX;
        RescheduleForm form = mock(RescheduleForm.class);
        given(form.getScheduleId()).willReturn(scheduleId);
        given(form.getOptionId()).willReturn(optionId);
        given(form.getRescheduleFormOption()).willReturn(
                RescheduleFormOption.PERMANENTLY);
        given(form.getLocalDate()).willReturn(date);

        Auditorium auditorium = new Auditorium("");
        Course course = new Course("");
        Group group = new Group("");
        Professor professor = new Professor("", "");
        Schedule schedule =
                new Schedule(scheduleId, scheduleId, date, DayOfWeek.MONDAY,
                        Period.FIRST, auditorium, course, group, professor);
        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        ReschedulingOption option = mock(ReschedulingOption.class);
        given(timetableFacade.getOption(anyLong())).willReturn(
                Optional.of(option));

        List<Schedule> affected = Collections.singletonList(schedule);
        given(timetableFacade.reschedulePermanently(any(Schedule.class),
                any(LocalDate.class), any(ReschedulingOption.class))).willThrow(
                new ServiceException(""));

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
        assertThatExceptionOfType(ServiceException.class).isThrownBy(
                () -> timetableFacade.reschedulePermanently(schedule, date,
                        option));
    }

}