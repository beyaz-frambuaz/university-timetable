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

@WebMvcTest(GroupsRestController.class)
@Import(ControllersTestConfig.class)
class GroupsRestControllerTest {

    private final String baseUrl = "/api/v1/timetable/groups/";
    private Group group;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private TimetableFacade timetableFacade;

    @BeforeEach
    private void setUp() {

        this.group = new Group(1L, "group");
    }

    @Test
    void findAllShouldRequestFromService() throws Exception {

        given(timetableFacade.getGroups()).willReturn(
                Collections.singletonList(group));

        mvc.perform(get(baseUrl).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.groupList[0].id").value(
                        group.getId()))
                .andExpect(jsonPath("$._embedded.groupList[0].name").value(
                        group.getName()))
                .andExpect(jsonPath(
                        "$._embedded.groupList[0]._links").isNotEmpty())
                .andExpect(jsonPath("$._links.self").isNotEmpty());

        then(timetableFacade).should().getGroups();
    }

    @Test
    void addNewShouldValidateAndReturnErrorsIfInvalid() throws Exception {

        group.setId(0L);
        group.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(group);

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
        String payload = objectMapper.writeValueAsString(group);

        given(timetableFacade.saveGroup(any(Group.class))).willReturn(group);

        mvc.perform(post(baseUrl).contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value(group.getName()))
                .andExpect(jsonPath("$._links").exists());

        then(timetableFacade).should().saveGroup(group);
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

        given(timetableFacade.getGroup(anyLong())).willReturn(
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
        then(timetableFacade).should().getGroup(999L);
    }

    @Test
    void findByIdShouldRequestFromService() throws Exception {

        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));

        mvc.perform(get(baseUrl + group.getId()).accept(
                MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value(group.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getGroup(group.getId());
    }

    @Test
    void editByIdShouldValidatePathVariableAndReturnErrorsIfInvalid()
            throws Exception {

        long invalidID = 0L;
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(group);

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

        group.setId(0L);
        group.setName(" ");
        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(group);

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
        String payload = objectMapper.writeValueAsString(group);

        given(timetableFacade.getGroup(anyLong())).willReturn(
                Optional.of(group));
        given(timetableFacade.saveGroup(any(Group.class))).willReturn(group);

        mvc.perform(put(baseUrl + group.getId()).contentType(
                MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(group.getId()))
                .andExpect(jsonPath("$.name").value(group.getName()))
                .andExpect(jsonPath("$._links.self").isNotEmpty())
                .andExpect(jsonPath("$._links.collection").isNotEmpty());

        then(timetableFacade).should().getGroup(group.getId());
        then(timetableFacade).should().saveGroup(group);
    }

    @Test
    void deleteByIdShouldReturnNotImplemented() throws Exception {

        mvc.perform(delete(baseUrl + group.getId()))
                .andExpect(status().isNotImplemented());
    }

}