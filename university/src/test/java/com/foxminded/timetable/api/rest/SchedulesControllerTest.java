package com.foxminded.timetable.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxminded.timetable.config.ControllersTestConfig;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolationException;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchedulesController.class)
@Import(ControllersTestConfig.class)
class SchedulesControllerTest {

    private final String baseUrl = "/api/v1/timetable/schedules/";
    private Schedule schedule;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;

    @BeforeEach
    private void setUp() {

        Auditorium auditorium = new Auditorium(2L, "auditorium");
        Course course = new Course(3L, "course");
        Group group = new Group(4L, "group");
        Professor professor = new Professor(5L, "professor", "professor");
        ScheduleTemplate template =
                new ScheduleTemplate(6L, true, DayOfWeek.MONDAY, Period.FIRST,
                        auditorium, course, group, professor);
        this.schedule = new Schedule(template, LocalDate.MAX);
        schedule.setId(1L);
    }

    @Test
    void findByDateShouldValidateAndReturnErrorsIfAnyIdsInvalid()
            throws Exception {

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + "date").accept(MediaType.APPLICATION_JSON)
                        .param("date", "2020-01-01")
                        .param("professorId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findByDateShouldRequestAuditoriumScheduleFromServiceIfAuditoriumIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateAuditoriumId(
                schedule.getAuditorium().getId());

        mvc.perform(get(baseUrl + "date").accept(MediaType.APPLICATION_JSON)
                .param("date", LocalDate.MAX.toString())
                .param("auditoriumId",
                        schedule.getAuditorium().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, LocalDate.MAX, LocalDate.MAX);
    }

    @Test
    void findByDateShouldRequestCourseScheduleFromServiceIfCourseIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate =
                new SchedulePredicateCourseId(schedule.getCourse().getId());

        mvc.perform(get(baseUrl + "date").accept(MediaType.APPLICATION_JSON)
                .param("date", LocalDate.MAX.toString())
                .param("courseId", schedule.getCourse().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, LocalDate.MAX, LocalDate.MAX);
    }

    @Test
    void findByDateShouldRequestGroupScheduleFromServiceIfGroupIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate =
                new SchedulePredicateGroupId(schedule.getGroup().getId());

        mvc.perform(get(baseUrl + "date").accept(MediaType.APPLICATION_JSON)
                .param("date", LocalDate.MAX.toString())
                .param("groupId", schedule.getGroup().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, LocalDate.MAX, LocalDate.MAX);
    }

    @Test
    void findByDateShouldRequestProfessorScheduleFromServiceIfProfessorIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateProfessorId(
                schedule.getProfessor().getId());

        mvc.perform(get(baseUrl + "date").accept(MediaType.APPLICATION_JSON)
                .param("date", LocalDate.MAX.toString())
                .param("professorId",
                        schedule.getProfessor().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, LocalDate.MAX, LocalDate.MAX);
    }

    @Test
    void findByDateShouldRequestUnfilteredScheduleFromServiceIfNoIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateNoFilter();

        mvc.perform(get(baseUrl + "date").accept(MediaType.APPLICATION_JSON)
                .param("date", LocalDate.MAX.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, LocalDate.MAX, LocalDate.MAX);
    }

    @Test
    void findByWeekShouldValidateAndReturnErrorsIfAnyIdsInvalid()
            throws Exception {

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + "week").accept(MediaType.APPLICATION_JSON)
                        .param("week", "0")
                        .param("professorId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findByWeekShouldRequestAuditoriumScheduleFromServiceIfAuditoriumIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateAuditoriumId(
                schedule.getAuditorium().getId());
        LocalDate monday = LocalDate.of(2020, 9, 7);
        LocalDate friday = LocalDate.of(2020, 9, 11);

        mvc.perform(get(baseUrl + "week").accept(MediaType.APPLICATION_JSON)
                .param("week", "1")
                .param("auditoriumId",
                        schedule.getAuditorium().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, monday, friday);
    }

    @Test
    void findByWeekShouldRequestCourseScheduleFromServiceIfCourseIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate =
                new SchedulePredicateCourseId(schedule.getCourse().getId());
        LocalDate monday = LocalDate.of(2020, 9, 7);
        LocalDate friday = LocalDate.of(2020, 9, 11);

        mvc.perform(get(baseUrl + "week").accept(MediaType.APPLICATION_JSON)
                .param("week", "1")
                .param("courseId", schedule.getCourse().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, monday, friday);
    }

    @Test
    void findByWeekShouldRequestGroupScheduleFromServiceIfGroupIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate =
                new SchedulePredicateGroupId(schedule.getGroup().getId());
        LocalDate monday = LocalDate.of(2020, 9, 7);
        LocalDate friday = LocalDate.of(2020, 9, 11);

        mvc.perform(get(baseUrl + "week").accept(MediaType.APPLICATION_JSON)
                .param("week", "1")
                .param("groupId", schedule.getGroup().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, monday, friday);
    }

    @Test
    void findByWeekShouldRequestProfessorScheduleFromServiceIfProfessorIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateProfessorId(
                schedule.getProfessor().getId());
        LocalDate monday = LocalDate.of(2020, 9, 7);
        LocalDate friday = LocalDate.of(2020, 9, 11);

        mvc.perform(get(baseUrl + "week").accept(MediaType.APPLICATION_JSON)
                .param("week", "1")
                .param("professorId",
                        schedule.getProfessor().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, monday, friday);
    }

    @Test
    void findByWeekShouldRequestUnfilteredScheduleFromServiceIfNoIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateNoFilter();
        LocalDate monday = LocalDate.of(2020, 9, 7);
        LocalDate friday = LocalDate.of(2020, 9, 11);

        mvc.perform(get(baseUrl + "week").accept(MediaType.APPLICATION_JSON)
                .param("week", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, monday, friday);
    }

    @Test
    void findByMonthShouldValidateAndReturnErrorsIfAnyIdsInvalid()
            throws Exception {

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + "month").accept(MediaType.APPLICATION_JSON)
                        .param("month", "0")
                        .param("professorId", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findByMonthShouldRequestAuditoriumScheduleFromServiceIfAuditoriumIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateAuditoriumId(
                schedule.getAuditorium().getId());
        LocalDate firstSemesterDateOfMonth = LocalDate.of(2020, 9, 7);
        LocalDate lastSemesterDateOfMonth = LocalDate.of(2020, 9, 30);

        mvc.perform(get(baseUrl + "month").accept(MediaType.APPLICATION_JSON)
                .param("month", "9")
                .param("auditoriumId",
                        schedule.getAuditorium().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, firstSemesterDateOfMonth,
                        lastSemesterDateOfMonth);
    }

    @Test
    void findByMonthShouldRequestCourseScheduleFromServiceIfCourseIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate =
                new SchedulePredicateCourseId(schedule.getCourse().getId());
        LocalDate firstSemesterDateOfMonth = LocalDate.of(2020, 9, 7);
        LocalDate lastSemesterDateOfMonth = LocalDate.of(2020, 9, 30);

        mvc.perform(get(baseUrl + "month").accept(MediaType.APPLICATION_JSON)
                .param("month", "9")
                .param("courseId", schedule.getCourse().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, firstSemesterDateOfMonth,
                        lastSemesterDateOfMonth);
    }

    @Test
    void findByMonthShouldRequestGroupScheduleFromServiceIfGroupIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate =
                new SchedulePredicateGroupId(schedule.getGroup().getId());
        LocalDate firstSemesterDateOfMonth = LocalDate.of(2020, 9, 7);
        LocalDate lastSemesterDateOfMonth = LocalDate.of(2020, 9, 30);

        mvc.perform(get(baseUrl + "month").accept(MediaType.APPLICATION_JSON)
                .param("month", "9")
                .param("groupId", schedule.getGroup().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, firstSemesterDateOfMonth,
                        lastSemesterDateOfMonth);
    }

    @Test
    void findByMonthShouldRequestProfessorScheduleFromServiceIfProfessorIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateProfessorId(
                schedule.getProfessor().getId());
        LocalDate firstSemesterDateOfMonth = LocalDate.of(2020, 9, 7);
        LocalDate lastSemesterDateOfMonth = LocalDate.of(2020, 9, 30);

        mvc.perform(get(baseUrl + "month").accept(MediaType.APPLICATION_JSON)
                .queryParam("month", "9")
                .param("professorId",
                        schedule.getProfessor().getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, firstSemesterDateOfMonth,
                        lastSemesterDateOfMonth);
    }

    @Test
    void findByMonthShouldRequestUnfilteredScheduleFromServiceIfNoIdProvided()
            throws Exception {

        given(timetableFacade.getScheduleFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));
        SchedulePredicate predicate = new SchedulePredicateNoFilter();
        LocalDate firstSemesterDateOfMonth = LocalDate.of(2020, 9, 7);
        LocalDate lastSemesterDateOfMonth = LocalDate.of(2020, 9, 30);

        mvc.perform(get(baseUrl + "month").accept(MediaType.APPLICATION_JSON)
                .param("month", "9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should()
                .getScheduleFor(predicate, firstSemesterDateOfMonth,
                        lastSemesterDateOfMonth);
    }

    @Test
    void findByIdShouldValidateAndReturnErrorsIfInvalid() throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findByIdShouldRequestFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getSchedule(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        mvc.perform(get(baseUrl + schedule.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(schedule.getId()))
                .andExpect(
                        jsonPath("$.date").value(schedule.getDate().toString()))
                .andExpect(
                        jsonPath("$.day").value(schedule.getDay().toString()))
                .andExpect(
                        jsonPath("$.period").value(schedule.getPeriod().name()))
                .andExpect(jsonPath("$.auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$.course").value(schedule.getCourse()))
                .andExpect(jsonPath("$.group").value(schedule.getGroup()))
                .andExpect(jsonPath("$.professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath("$.professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath("$.professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
    }

    @Test
    void rescheduleShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;
        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(schedule);

        MvcResult mvcResult = mvc.perform(
                put(baseUrl + invalidID).contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void rescheduleShouldValidatePayloadAndReturnErrorsIfInvalid()
            throws Exception {

        schedule.setId(-1L);
        schedule.getAuditorium().setId(0L);
        schedule.getAuditorium().setName(" ");

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(schedule);

        MvcResult mvcResult = mvc.perform(
                put(baseUrl + 1L).contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<MethodArgumentNotValidException> exception =
                Optional.ofNullable(
                        (MethodArgumentNotValidException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(MethodArgumentNotValidException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void rescheduleShouldRequestExistingScheduleFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(schedule);

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                put(baseUrl + schedule.getId()).contentType(
                        MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getSchedule(schedule.getId());
    }

    @Test
    void rescheduleShouldValidatePayloadForConsistencyAndReturnErrorsIfInvalid()
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(schedule);

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.isValidToReschedule(
                any(Schedule.class))).willReturn(false);

        MvcResult mvcResult = mvc.perform(
                put(baseUrl + schedule.getId()).contentType(
                        MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ApiException> exception = Optional.ofNullable(
                (ApiException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ApiException.class);
        then(timetableFacade).should().getSchedule(schedule.getId());
    }

    @Test
    void rescheduleShouldDelegateSaveScheduleToServiceIfNotRecurring()
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(schedule);

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.isValidToReschedule(
                any(Schedule.class))).willReturn(true);
        given(timetableFacade.saveSchedule(any(Schedule.class))).willReturn(
                schedule);

        mvc.perform(put(baseUrl + schedule.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).should().saveSchedule(any());
    }

    @Test
    void rescheduleShouldDelegateRescheduleRecurringToServiceIfRequestedRecurringTrue()
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        String payload = mapper.writeValueAsString(schedule);

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.isValidToReschedule(
                any(Schedule.class))).willReturn(true);
        given(timetableFacade.rescheduleRecurring(any(Schedule.class),
                any(LocalDate.class),
                any(ReschedulingOption.class))).willReturn(
                Collections.singletonList(schedule));

        mvc.perform(put(baseUrl + schedule.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload).queryParam(
                        "recurring", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.scheduleList[0].id").value(
                        schedule.getId()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].date").value(
                        schedule.getDate().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].day").value(
                        schedule.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].period").value(
                        schedule.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].auditorium").value(
                        schedule.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].course").value(
                        schedule.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleList[0].group").value(
                        schedule.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0].professor.lastName").value(
                        schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).should().rescheduleRecurring(any(), any(), any());
    }

    @Test
    void findAvailableAuditoriumsShouldValidateAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID + "/available/auditoriums").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findAvailableAuditoriumsShouldRequestFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L + "/available/auditoriums").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getSchedule(999L);
    }

    @Test
    void findAvailableAuditoriumsShouldRequestFromService() throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.getAvailableAuditoriums(any(LocalDate.class),
                any(Period.class))).willReturn(
                Collections.singletonList(schedule.getAuditorium()));

        mvc.perform(get(baseUrl + schedule.getId()
                + "/available/auditoriums").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.auditoriumList[0].id").value(
                        schedule.getAuditorium().getId()))
                .andExpect(jsonPath("$._embedded.auditoriumList[0].name").value(
                        schedule.getAuditorium().getName()))
                .andExpect(jsonPath(
                        "$._embedded.auditoriumList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).should()
                .getAvailableAuditoriums(schedule.getDate(),
                        schedule.getPeriod());
    }

    @Test
    void findAvailableProfessorsShouldValidateAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID + "/available/professors").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findAvailableProfessorsShouldRequestFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L + "/available/professors").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getSchedule(999L);
    }

    @Test
    void findAvailableProfessorsShouldRequestFromService() throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        given(timetableFacade.getAvailableProfessors(any(LocalDate.class),
                any(Period.class))).willReturn(
                Collections.singletonList(schedule.getProfessor()));

        mvc.perform(get(baseUrl + schedule.getId()
                + "/available/professors").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.professorList[0].id").value(
                        schedule.getProfessor().getId()))
                .andExpect(jsonPath(
                        "$._embedded.professorList[0].firstName").value(
                        schedule.getProfessor().getFirstName()))
                .andExpect(
                        jsonPath("$._embedded.professorList[0].lastName").value(
                                schedule.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.professorList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).should()
                .getAvailableProfessors(schedule.getDate(),
                        schedule.getPeriod());
    }

    @Test
    void findOptionsShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID + "/options").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findOptionsShouldValidateWeekAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidWeek = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + schedule.getId() + "/options").param("week",
                        String.valueOf(invalidWeek))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findOptionsShouldRequestFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L + "/options").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getSchedule(999L);
    }

    @Test
    void findOptionsShouldReturnEmptyListWhenQueryParamsNotPresent()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));

        mvc.perform(get(baseUrl + schedule.getId() + "/options").accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").doesNotExist())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).shouldHaveNoMoreInteractions();
    }

    @Test
    void findOptionsShouldRequestDateOptionsFromServiceWhenDateQueryParamPresent()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        ReschedulingOption option =
                new ReschedulingOption(DayOfWeek.FRIDAY, Period.FIFTH,
                        new Auditorium(99L, "another auditorium"));
        given(timetableFacade.getOptionsForDate(any(Schedule.class),
                any(LocalDate.class))).willReturn(
                Collections.singletonList(option));

        mvc.perform(get(baseUrl + schedule.getId() + "/options").param("date",
                LocalDate.MIN.toString()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reschedulingOptionList[0].day")
                        .value(option.getDay().toString()))
                .andExpect(
                        jsonPath("$._embedded.reschedulingOptionList[0].period")
                                .value(option.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.reschedulingOptionList[0].auditorium").value(
                        option.getAuditorium()))
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).should()
                .getOptionsForDate(schedule, LocalDate.MIN);
    }

    @Test
    void findOptionsShouldRequestWeekOptionsFromServiceWhenWeekQueryParamPresent()
            throws Exception {

        given(timetableFacade.getSchedule(anyLong())).willReturn(
                Optional.of(schedule));
        ReschedulingOption option =
                new ReschedulingOption(DayOfWeek.FRIDAY, Period.FIFTH,
                        new Auditorium(99L, "another auditorium"));
        given(timetableFacade.getOptionsForWeek(any(Schedule.class),
                anyInt())).willReturn(Collections.singletonList(option));

        mvc.perform(get(baseUrl + schedule.getId() + "/options").param("week",
                String.valueOf(1)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.reschedulingOptionList[0].day")
                        .value(option.getDay().toString()))
                .andExpect(
                        jsonPath("$._embedded.reschedulingOptionList[0].period")
                                .value(option.getPeriod().name()))
                .andExpect(jsonPath(
                        "$._embedded.reschedulingOptionList[0].auditorium").value(
                        option.getAuditorium()))
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getSchedule(schedule.getId());
        then(timetableFacade).should().getOptionsForWeek(schedule, 1);
    }

}