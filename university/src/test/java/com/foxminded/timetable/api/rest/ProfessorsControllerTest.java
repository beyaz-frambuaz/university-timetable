package com.foxminded.timetable.api.rest;

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

@WebMvcTest(ProfessorsController.class)
@Import(ControllersTestConfig.class)
class ProfessorsControllerTest {

    private final String baseUrl = "/api/v1/timetable/professors/";
    private Professor professor;
    private Course course;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;

    @BeforeEach
    private void setUp() {

        this.course = new Course(1L, "course");
        this.professor = new Professor(1L, "first", "last");
        professor.addCourse(course);
    }

    @Test
    void findAllShouldRequestFromService() throws Exception {

        given(timetableFacade.getProfessors()).willReturn(
                Collections.singletonList(professor));

        mvc.perform(get(baseUrl).accept(MediaType.APPLICATION_JSON))
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
                        "$._embedded.professorList[0].courses[0].id").value(
                        course.getId()))
                .andExpect(
                        jsonPath("$._embedded.professorList[0].courses[0].name")
                                .value(course.getName()))
                .andExpect(jsonPath(
                        "$._embedded.professorList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getProfessors();
    }

    @Test
    void addNewShouldValidateAndReturnErrorsIfInvalid() throws Exception {

        professor.setId(0L);
        professor.setFirstName(" ");
        professor.setLastName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(professor);

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
        String payload = objectMapper.writeValueAsString(professor);

        given(timetableFacade.saveProfessor(any(Professor.class))).willReturn(
                professor);

        mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(professor.getId()))
                .andExpect(
                        jsonPath("$.firstName").value(professor.getFirstName()))
                .andExpect(
                        jsonPath("$.lastName").value(professor.getLastName()))
                .andExpect(jsonPath("$.courses[0].id").value(course.getId()))
                .andExpect(
                        jsonPath("$.courses[0].name").value(course.getName()))
                .andExpect(jsonPath("$._links").exists());

        then(timetableFacade).should().saveProfessor(professor);
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

        given(timetableFacade.getProfessor(anyLong())).willReturn(
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
        then(timetableFacade).should().getProfessor(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));

        mvc.perform(get(baseUrl + professor.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professor.getId()))
                .andExpect(
                        jsonPath("$.firstName").value(professor.getFirstName()))
                .andExpect(
                        jsonPath("$.lastName").value(professor.getLastName()))
                .andExpect(jsonPath("$.courses[0].id").value(course.getId()))
                .andExpect(
                        jsonPath("$.courses[0].name").value(course.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getProfessor(professor.getId());
    }

    @Test
    void editByIdShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(professor);

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

        professor.setId(0L);
        professor.setFirstName(" ");
        professor.setLastName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(professor);

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
        String payload = objectMapper.writeValueAsString(professor);

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));
        given(timetableFacade.saveProfessor(any(Professor.class))).willReturn(
                professor);

        mvc.perform(put(baseUrl + professor.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(professor.getId()))
                .andExpect(
                        jsonPath("$.firstName").value(professor.getFirstName()))
                .andExpect(
                        jsonPath("$.lastName").value(professor.getLastName()))
                .andExpect(jsonPath("$.courses[0].id").value(course.getId()))
                .andExpect(
                        jsonPath("$.courses[0].name").value(course.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getProfessor(professor.getId());
        then(timetableFacade).should().saveProfessor(professor);
    }

    @Test
    void deleteByIdShouldReturnNotImplemented() throws Exception {

        mvc.perform(delete(baseUrl + professor.getId()))
                .andExpect(status().isNotImplemented());
    }

    @Test
    void findProfessorCoursesShouldValidateAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID + "/courses").accept(
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
    void findProfessorCoursesShouldRequestFromServiceAndThrowNotFoundExceptionIfNotFound()
            throws Exception {

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L + "/courses").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getProfessor(999L);
    }

    @Test
    void findProfessorCoursesShouldRequestFromService() throws Exception {

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));

        mvc.perform(get(baseUrl + professor.getId() + "/courses").accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.courseList[0].id").value(
                        course.getId()))
                .andExpect(jsonPath("$._embedded.courseList[0].name").value(
                        course.getName()))
                .andExpect(jsonPath(
                        "$._embedded.courseList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getProfessor(professor.getId());
    }

    @Test
    void findCourseAttendeesShouldValidateProfessorIdAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + invalidID + "/courses/" + course.getId()
                        + "/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findCourseAttendeesShouldValidateCourseIdAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + professor.getId() + "/courses/" + invalidID
                        + "/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(ConstraintViolationException.class);
        then(timetableFacade).shouldHaveNoInteractions();
    }

    @Test
    void findCourseAttendeesShouldRequestFromServiceAndThrowNotFoundExceptionIfProfessorNotFound()
            throws Exception {

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.empty());

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + 999L + "/courses/1/students").accept(
                        MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getProfessor(999L);
        then(timetableFacade).shouldHaveNoMoreInteractions();
    }

    @Test
    void findCourseAttendeesShouldRequestFromServiceAndThrowNotFoundExceptionIfCourseNotFound()
            throws Exception {

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + professor.getId() + "/courses/" + 999L
                        + "/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.errors").isNotEmpty())
                .andReturn();

        Optional<NotFoundException> exception = Optional.ofNullable(
                (NotFoundException) mvcResult.getResolvedException());

        assertThat(exception).isNotEmpty()
                .containsInstanceOf(NotFoundException.class);
        then(timetableFacade).should().getProfessor(professor.getId());
        then(timetableFacade).shouldHaveNoMoreInteractions();
    }

    @Test
    void findCourseAttendeesShouldRequestFromService() throws Exception {

        given(timetableFacade.getProfessor(anyLong())).willReturn(
                Optional.of(professor));
        Student student =
                new Student(5L, "student", "student", new Group(6L, "group"));
        given(timetableFacade.getCourseAttendees(any(Course.class),
                any(Professor.class))).willReturn(
                Collections.singletonList(student));

        mvc.perform(
                get(baseUrl + professor.getId() + "/courses/" + course.getId()
                        + "/students").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.studentList[0].id").value(
                        student.getId()))
                .andExpect(
                        jsonPath("$._embedded.studentList[0].firstName").value(
                                student.getFirstName()))
                .andExpect(
                        jsonPath("$._embedded.studentList[0].lastName").value(
                                student.getLastName()))
                .andExpect(
                        jsonPath("$._embedded.studentList[0].group.id").value(
                                student.getGroup().getId()))
                .andExpect(
                        jsonPath("$._embedded.studentList[0].group.name").value(
                                student.getGroup().getName()))
                .andExpect(jsonPath(
                        "$._embedded.studentList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links").isNotEmpty());

        then(timetableFacade).should().getProfessor(professor.getId());
        then(timetableFacade).should().getCourseAttendees(course, professor);
    }

}