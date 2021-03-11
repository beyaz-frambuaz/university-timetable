package com.shablii.timetable.mvc;

import com.shablii.timetable.config.ControllersTestConfig;
import com.shablii.timetable.forms.*;
import com.shablii.timetable.forms.utility.*;
import com.shablii.timetable.forms.utility.formatter.ScheduleFormatter;
import com.shablii.timetable.model.Auditorium;
import com.shablii.timetable.service.TimetableFacade;
import com.shablii.timetable.service.utility.predicates.*;
import org.junit.jupiter.api.Test;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllersTestConfig.class)
@WebMvcTest(ManagementUniversityAuditoriumsController.class)
class ManagementUniversityAuditoriumsControllerTest {

    private final String baseUrl = "/timetable/management/university/auditoriums";
    private final String baseView = "management/university/auditoriums";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private TimetableFacade timetableFacade;

    @Test
    public void getAuditoriumsShouldRequestFromServiceAndDisplay() throws Exception {

        List<Auditorium> auditoriums = Collections.emptyList();
        given(timetableFacade.getAuditoriums()).willReturn(auditoriums);

        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(model().attribute("auditoriums", auditoriums))
                .andExpect(
                        model().attributeExists("errorAlert", "successAlert", "editedId", "renameForm", "newItemForm"))
                .andExpect(view().name(baseView + "/auditoriums"));

        then(timetableFacade).should().getAuditoriums();
    }

    @Test
    public void postScheduleShouldValidateFormAndRedirectToAuditoriumsWithErrorMessageIfInvalid() throws Exception {

        long id = 0L;
        String date = "invalid date";

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date);
        scheduleForm.setId(id);

        RequestBuilder requestBuilder = post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postScheduleShouldRequestAuditoriumFromServiceAndRedirectToAuditoriumsIfNotPresent() throws Exception {

        long id = 1L;
        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate("2020-06-01");
        scheduleForm.setId(id);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getAuditorium(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest() throws Exception {

        long id = 1L;
        Auditorium auditorium = mock(Auditorium.class);
        given(auditorium.getId()).willReturn(id);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.of(auditorium));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateAuditoriumId(id);

        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(daySchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(model().attribute("auditorium", auditorium))
                .andExpect(view().name(baseView + "/schedule/day"));

        then(scheduleFormatter).should().prepareDaySchedule(predicate, date, filtered);
        then(timetableFacade).should().getAuditorium(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest() throws Exception {

        long id = 1L;
        Auditorium auditorium = mock(Auditorium.class);
        given(auditorium.getId()).willReturn(id);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.of(auditorium));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateAuditoriumId(id);

        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(model().attribute("auditorium", auditorium))
                .andExpect(view().name(baseView + "/schedule/week"));

        then(scheduleFormatter).should().prepareWeekSchedule(predicate, date, filtered);
        then(timetableFacade).should().getAuditorium(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest() throws Exception {

        long id = 1L;
        Auditorium auditorium = mock(Auditorium.class);
        given(auditorium.getId()).willReturn(id);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.of(auditorium));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateAuditoriumId(id);

        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(model().attribute("auditorium", auditorium))
                .andExpect(view().name(baseView + "/schedule/month"));

        then(scheduleFormatter).should().prepareMonthSchedule(predicate, date, filtered);
        then(timetableFacade).should().getAuditorium(id);
    }

    @Test
    public void getScheduleShouldRedirectToAuditoriums() throws Exception {

        mvc.perform(get(baseUrl + "/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(baseUrl));
    }

    @Test
    public void postRenameShouldValidateFormAndRedirectToAuditoriumsWithErrorMessageIfInvalid() throws Exception {

        RenameForm renameForm = new RenameForm();
        renameForm.setNewName(" ");
        renameForm.setRenameId(0L);

        RequestBuilder requestBuilder = post(baseUrl + "/rename").flashAttr("renameForm", renameForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postRenameShouldRequestAuditoriumFromServiceAndRedirectToAuditoriumsIfNotPresent() throws Exception {

        long id = 1L;
        RenameForm form = new RenameForm();
        form.setRenameId(id);
        form.setNewName("test");
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/rename").flashAttr("renameForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getAuditorium(id);
    }

    @Test
    public void postRenameShouldSetAuditoriumNameRequestServiceToSaveAndRedirectToAuditoriumsWithMessage()
            throws Exception {

        Auditorium auditorium = mock(Auditorium.class);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.of(auditorium));
        long id = 1L;
        String name = "name";
        given(auditorium.getId()).willReturn(id);
        given(auditorium.getName()).willReturn(name);

        RenameForm form = new RenameForm();
        form.setRenameId(id);
        form.setNewName(name);

        mvc.perform(post(baseUrl + "/rename").flashAttr("renameForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getAuditorium(id);
        then(auditorium).should().setName(name);
        then(timetableFacade).should().saveAuditorium(auditorium);
    }

    @Test
    public void getRemoveShouldValidateIdAndRedirectToAuditoriumsIfInvalid() throws Exception {

        long id = 0L;

        MvcResult mvcResult = mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void getRemoveShouldRequestAuditoriumFromServiceAndRedirectToAuditoriumsIfNotPresent() throws Exception {

        long id = 1L;
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.empty());

        mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getAuditorium(id);
    }

    @Test
    public void getRemoveShouldRequestServiceToDeleteAndRedirectToAuditoriumsWithMessage() throws Exception {

        long id = 1L;
        Auditorium auditorium = mock(Auditorium.class);
        given(timetableFacade.getAuditorium(anyLong())).willReturn(Optional.of(auditorium));

        mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getAuditorium(id);
        then(timetableFacade).should().deleteAuditorium(auditorium);
    }

    @Test
    public void postNewShouldValidateFormAndRedirectToAuditoriumsWithErrorMessageIfInvalid() throws Exception {

        NewItemForm newItemForm = new NewItemForm();
        newItemForm.setName(" ");

        RequestBuilder requestBuilder = post(baseUrl + "/new").flashAttr("newItemForm", newItemForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postNewShouldCreateAuditoriumRequestServiceToSaveAndRedirectToAuditoriumsWithMessage()
            throws Exception {

        Auditorium savedAuditorium = mock(Auditorium.class);
        long id = 1L;
        String name = "name";
        given(savedAuditorium.getId()).willReturn(id);
        given(savedAuditorium.getName()).willReturn(name);
        given(timetableFacade.saveAuditorium(any(Auditorium.class))).willReturn(savedAuditorium);

        Auditorium newAuditorium = new Auditorium(name);

        NewItemForm form = new NewItemForm();
        form.setName(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newItemForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().saveAuditorium(newAuditorium);
    }

}