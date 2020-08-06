package com.foxminded.timetable.rest;

import com.foxminded.timetable.config.ControllersTestConfig;
import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableFacade;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import javax.validation.ConstraintViolationException;
import java.time.DayOfWeek;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TemplatesRestController.class)
@Import(ControllersTestConfig.class)
class TemplatesRestControllerTest {

    private final String baseUrl = "/api/v1/timetable/templates/";
    private ScheduleTemplate template;

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
        this.template =
                new ScheduleTemplate(1L, true, DayOfWeek.MONDAY, Period.FIRST,
                        auditorium, course, group, professor);
    }

    @Test
    void findAllShouldRequestFromService() throws Exception {

        given(timetableFacade.getTwoWeekSchedule()).willReturn(
                Collections.singletonList(template));

        mvc.perform(get(baseUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$._embedded.scheduleTemplateList[0].id").value(
                        template.getId()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0]"
                        + ".weekParity").value(template.getWeekParity()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleTemplateList[0].day").value(
                        template.getDay().toString()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0]"
                        + ".period").value(template.getPeriod().name()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0]"
                        + ".auditorium").value(template.getAuditorium()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0]"
                        + ".course").value(template.getCourse()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0].group")
                        .value(template.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleTemplateList[0].professor.id").value(
                        template.getProfessor().getId()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0]"
                        + ".professor.firstName").value(
                        template.getProfessor().getFirstName()))
                .andExpect(jsonPath("$._embedded.scheduleTemplateList[0]"
                        + ".professor.lastName").value(
                        template.getProfessor().getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.scheduleTemplateList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getTwoWeekSchedule();
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

        given(timetableFacade.getTemplate(anyLong())).willReturn(
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
        then(timetableFacade).should().getTemplate(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getTemplate(anyLong())).willReturn(
                Optional.of(template));

        mvc.perform(get(baseUrl + template.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(template.getId()))
                .andExpect(jsonPath("$.weekParity").value(
                        template.getWeekParity()))
                .andExpect(
                        jsonPath("$.day").value(template.getDay().toString()))
                .andExpect(
                        jsonPath("$.period").value(template.getPeriod().name()))
                .andExpect(jsonPath("$.auditorium").value(
                        template.getAuditorium()))
                .andExpect(jsonPath("$.course").value(template.getCourse()))
                .andExpect(jsonPath("$.group").value(template.getGroup()))
                .andExpect(jsonPath("$.professor.id").value(
                        template.getProfessor().getId()))
                .andExpect(jsonPath("$.professor.firstName").value(
                        template.getProfessor().getFirstName()))
                .andExpect(jsonPath("$.professor.lastName").value(
                        template.getProfessor().getLastName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getTemplate(template.getId());
    }

}