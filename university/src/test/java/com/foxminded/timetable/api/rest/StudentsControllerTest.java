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

@WebMvcTest(StudentsController.class)
@Import(ControllersTestConfig.class)
class StudentsControllerTest {

    private final String baseUrl = "/api/v1/timetable/students/";
    private Student student;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;

    @BeforeEach
    private void setUp() {

        this.student = new Student(1L, "first", "last", new Group(2L, "group"));
    }

    @Test
    void findAllShouldRequestFromService() throws Exception {

        given(timetableFacade.getStudents()).willReturn(
                Collections.singletonList(student));

        mvc.perform(get(baseUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.studentList[0].id").value(
                        student.getId()))
                .andExpect(
                        jsonPath("$._embedded.studentList[0].firstName").value(
                                student.getFirstName()))
                .andExpect(
                        jsonPath("$._embedded.studentList[0].lastName").value(
                                student.getLastName()))
                .andExpect(jsonPath("$._embedded.studentList[0].group").value(
                        student.getGroup()))
                .andExpect(jsonPath(
                        "$._embedded.studentList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getStudents();
    }

    @Test
    void addNewShouldValidateAndReturnErrorsIfInvalid() throws Exception {

        student.setId(0L);
        student.setFirstName(" ");
        student.setLastName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(student);

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
        String payload = objectMapper.writeValueAsString(student);

        given(timetableFacade.saveStudent(any(Student.class))).willReturn(
                student);

        mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(
                        jsonPath("$.firstName").value(student.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(student.getLastName()))
                .andExpect(jsonPath("$.group").value(student.getGroup()))
                .andExpect(jsonPath("$._links").exists());

        then(timetableFacade).should().saveStudent(student);
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

        given(timetableFacade.getStudent(anyLong())).willReturn(
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
        then(timetableFacade).should().getStudent(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.of(student));

        mvc.perform(get(baseUrl + student.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(
                        jsonPath("$.firstName").value(student.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(student.getLastName()))
                .andExpect(jsonPath("$.group").value(student.getGroup()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getStudent(student.getId());
    }

    @Test
    void editByIdShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(student);

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

        student.setId(0L);
        student.setFirstName(" ");
        student.setLastName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(student);

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
        String payload = objectMapper.writeValueAsString(student);

        given(timetableFacade.getStudent(anyLong())).willReturn(
                Optional.of(student));
        given(timetableFacade.saveStudent(any(Student.class))).willReturn(
                student);

        mvc.perform(put(baseUrl + student.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(student.getId()))
                .andExpect(
                        jsonPath("$.firstName").value(student.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(student.getLastName()))
                .andExpect(jsonPath("$.group").value(student.getGroup()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getStudent(student.getId());
        then(timetableFacade).should().saveStudent(student);
    }

    @Test
    void deleteByIdShouldReturnNotImplemented() throws Exception {

        mvc.perform(delete(baseUrl + student.getId()))
                .andExpect(status().isNotImplemented());
    }

}