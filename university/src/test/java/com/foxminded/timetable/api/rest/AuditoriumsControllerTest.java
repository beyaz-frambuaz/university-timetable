package com.foxminded.timetable.api.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foxminded.timetable.api.AuditoriumsApi;
import com.foxminded.timetable.config.ControllersTestConfig;
import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.model.Auditorium;
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
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuditoriumsApi.class)
@Import(ControllersTestConfig.class)
class AuditoriumsControllerTest {

    private final String baseUrl = "/api/v1/timetable/auditoriums/";
    private Auditorium auditorium;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;

    @BeforeEach
    private void setUp() {

        this.auditorium = new Auditorium(1L, "auditorium");
    }

    @Test
    void findAllShouldRequestFromService() throws Exception {

        given(timetableFacade.getAuditoriums()).willReturn(
                Collections.singletonList(auditorium));

        mvc.perform(get(baseUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.auditoriumList[0].id").value(
                        auditorium.getId()))
                .andExpect(jsonPath("$._embedded.auditoriumList[0].name").value(
                        auditorium.getName()))
                .andExpect(jsonPath(
                        "$._embedded.auditoriumList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getAuditoriums();
    }

    @Test
    void addNewShouldValidateAndReturnErrorsIfInvalid() throws Exception {

        auditorium.setId(0L);
        auditorium.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(auditorium);

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
        String payload = objectMapper.writeValueAsString(auditorium);

        given(timetableFacade.saveAuditorium(any(Auditorium.class))).willReturn(
                auditorium);

        mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(auditorium.getId()))
                .andExpect(jsonPath("$.name").value(auditorium.getName()))
                .andExpect(jsonPath("$._links").exists());

        then(timetableFacade).should().saveAuditorium(auditorium);
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

        given(timetableFacade.getAuditorium(anyLong())).willReturn(
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
        then(timetableFacade).should().getAuditorium(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getAuditorium(anyLong())).willReturn(
                Optional.of(auditorium));

        mvc.perform(get(baseUrl + auditorium.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(auditorium.getId()))
                .andExpect(jsonPath("$.name").value(auditorium.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getAuditorium(auditorium.getId());
    }

    @Test
    void editByIdShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(auditorium);

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

        auditorium.setId(0L);
        auditorium.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(auditorium);

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
        String payload = objectMapper.writeValueAsString(auditorium);

        given(timetableFacade.getAuditorium(anyLong())).willReturn(
                Optional.of(auditorium));
        given(timetableFacade.saveAuditorium(any(Auditorium.class))).willReturn(
                auditorium);

        mvc.perform(put(baseUrl + auditorium.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(auditorium.getId()))
                .andExpect(jsonPath("$.name").value(auditorium.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getAuditorium(auditorium.getId());
        then(timetableFacade).should().saveAuditorium(auditorium);
    }

    @Test
    void deleteByIdShouldReturnNotImplemented() throws Exception {

        mvc.perform(delete(baseUrl + auditorium.getId()))
                .andExpect(status().isNotImplemented());
    }

}