package com.foxminded.timetable.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.validation.ConstraintViolationException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoursesRestController.class)
@Import(ControllersTestConfig.class)
class CoursesRestControllerTest {

    private final String baseUrl = "/api/v1/timetable/courses/";
    private Course course;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;

    @BeforeEach
    private void setUp() {

        this.course = new Course(1L, "course");
    }

    @Test
    void findAllShouldRequestFromService() throws Exception {

        given(timetableFacade.getCourses()).willReturn(
                Collections.singletonList(course));

        mvc.perform(get(baseUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.courseList[0].id").value(
                        course.getId()))
                .andExpect(jsonPath("$._embedded.courseList[0].name").value(
                        course.getName()))
                .andExpect(jsonPath(
                        "$._embedded.courseList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getCourses();
    }

    @Test
    void addNewShouldValidateAndReturnErrorsIfInvalid() throws Exception {

        course.setId(0L);
        course.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(course);

        MvcResult mvcResult = mvc.perform(
                post(baseUrl).contentType(MediaType.APPLICATION_JSON)
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
    void addNewShouldSaveAndReturnCreated() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(course);

        given(timetableFacade.saveCourse(any(Course.class))).willReturn(course);

        mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(course.getId()))
                .andExpect(jsonPath("$.name").value(course.getName()))
                .andExpect(jsonPath("$._links").exists());

        then(timetableFacade).should().saveCourse(course);
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

        given(timetableFacade.getCourse(anyLong())).willReturn(
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
        then(timetableFacade).should().getCourse(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));

        mvc.perform(get(baseUrl + course.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(course.getId()))
                .andExpect(jsonPath("$.name").value(course.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getCourse(course.getId());
    }

    @Test
    void editByIdShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(course);

        MvcResult mvcResult = mvc.perform(
                put(baseUrl + invalidID).content(payload)
                        .contentType(MediaType.APPLICATION_JSON))
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
    void editByIdShouldValidatePayloadAndReturnErrorsIfInvalid()
            throws Exception {

        course.setId(0L);
        course.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(course);

        MvcResult mvcResult = mvc.perform(put(baseUrl + 1L).content(payload)
                .contentType(MediaType.APPLICATION_JSON))
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
    void editByIdShouldCallServiceToSave() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(course);

        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));
        given(timetableFacade.saveCourse(any(Course.class))).willReturn(course);

        mvc.perform(put(baseUrl + course.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(course.getId()))
                .andExpect(jsonPath("$.name").value(course.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getCourse(course.getId());
        then(timetableFacade).should().saveCourse(course);
    }

    @Test
    void deleteByIdShouldReturnNotImplemented() throws Exception {

        mvc.perform(delete(baseUrl + course.getId()))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void findCourseProfessorsShouldValidateAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID + "/professors").accept(
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
    void findCourseProfessorsShouldRequestFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L + "/professors").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getCourse(999L);
    }

    @Test
    void findCourseProfessorsShouldRequestFromService() throws Exception {

        Professor professor = new Professor(1L, "first", "last");
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));
        given(timetableFacade.getProfessorsTeaching(
                any(Course.class))).willReturn(
                Collections.singletonList(professor));

        mvc.perform(get(baseUrl + course.getId() + "/professors").accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.professorList[0].id").value(
                        professor.getId()))
                .andExpect(jsonPath(
                        "$._embedded.professorList[0].firstName").value(
                        professor.getFirstName()))
                .andExpect(
                        jsonPath("$._embedded.professorList[0].lastName").value(
                                professor.getLastName()))
                .andExpect(jsonPath(
                        "$._embedded.professorList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getCourse(course.getId());
        then(timetableFacade).should().getProfessorsTeaching(course);
    }

}