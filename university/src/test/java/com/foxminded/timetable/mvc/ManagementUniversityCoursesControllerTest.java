package com.foxminded.timetable.mvc;

import com.foxminded.timetable.config.ControllersTestConfig;
import com.foxminded.timetable.forms.*;
import com.foxminded.timetable.forms.utility.*;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.*;
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
@WebMvcTest(ManagementUniversityCoursesController.class)
class ManagementUniversityCoursesControllerTest {

    private final String baseUrl = "/timetable/management/university/courses";
    private final String baseView = "management/university/courses";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private TimetableFacade timetableFacade;

    @Test
    public void getCoursesShouldRequestFromServiceAndDisplay()
            throws Exception {

        List<Course> courses = Collections.emptyList();
        given(timetableFacade.getCourses()).willReturn(courses);

        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(model().attribute("courses", courses))
                .andExpect(model().attributeExists("errorAlert", "successAlert",
                        "editedId", "renameForm", "newItemForm"))
                .andExpect(view().name(baseView + "/courses"));

        then(timetableFacade).should().getCourses();
    }

    @Test
    public void postScheduleShouldValidateFormAndRedirectToCoursesWithErrorMessageIfInvalid()
            throws Exception {

        long id = 0L;
        String date = "invalid date";

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date);
        scheduleForm.setId(id);

        RequestBuilder requestBuilder =
                post(baseUrl + "/schedule").flashAttr("scheduleForm",
                        scheduleForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postScheduleShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent()
            throws Exception {

        long id = 1L;
        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate("2020-06-01");
        scheduleForm.setId(id);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getCourse(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest()
            throws Exception {

        long id = 1L;
        Course course = mock(Course.class);
        given(course.getId()).willReturn(id);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateCourseId(id);

        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class),
                any(LocalDate.class), anyBoolean())).willReturn(daySchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(model().attribute("course", course))
                .andExpect(view().name(baseView + "/schedule/day"));

        then(scheduleFormatter).should()
                .prepareDaySchedule(predicate, date, filtered);
        then(timetableFacade).should().getCourse(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest()
            throws Exception {

        long id = 1L;
        Course course = mock(Course.class);
        given(course.getId()).willReturn(id);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateCourseId(id);

        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(model().attribute("course", course))
                .andExpect(view().name(baseView + "/schedule/week"));

        then(scheduleFormatter).should()
                .prepareWeekSchedule(predicate, date, filtered);
        then(timetableFacade).should().getCourse(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest()
            throws Exception {

        long id = 1L;
        Course course = mock(Course.class);
        given(course.getId()).willReturn(id);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateCourseId(id);

        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(
                any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm",
                scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(model().attribute("course", course))
                .andExpect(view().name(baseView + "/schedule/month"));

        then(scheduleFormatter).should()
                .prepareMonthSchedule(predicate, date, filtered);
        then(timetableFacade).should().getCourse(id);
    }

    @Test
    public void getScheduleShouldRedirectToCourses() throws Exception {

        mvc.perform(get(baseUrl + "/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(baseUrl));
    }

    @Test
    public void postRenameShouldValidateFormAndRedirectToCoursesWithErrorMessageIfInvalid()
            throws Exception {

        RenameForm renameForm = new RenameForm();
        renameForm.setNewName(" ");
        renameForm.setRenameId(0L);

        RequestBuilder requestBuilder =
                post(baseUrl + "/rename").flashAttr("renameForm", renameForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postRenameShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent()
            throws Exception {

        long id = 1L;
        RenameForm form = new RenameForm();
        form.setRenameId(id);
        form.setNewName("test");
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(post(baseUrl + "/rename").flashAttr("renameForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getCourse(id);
    }

    @Test
    public void postRenameShouldSetCourseNameRequestServiceToSaveAndRedirectToCoursesWithMessage()
            throws Exception {

        Course course = mock(Course.class);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));
        long id = 1L;
        String name = "name";
        given(course.getId()).willReturn(id);
        given(course.getName()).willReturn(name);

        RenameForm form = new RenameForm();
        form.setRenameId(id);
        form.setNewName(name);

        mvc.perform(post(baseUrl + "/rename").flashAttr("renameForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getCourse(id);
        then(course).should().setName(name);
        then(timetableFacade).should().saveCourse(course);
    }

    @Test
    public void getRemoveShouldValidateIdAndRedirectToCoursesIfInvalid()
            throws Exception {

        long id = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void getRemoveShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent()
            throws Exception {

        long id = 1L;
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getCourse(id);
    }

    @Test
    public void getRemoveShouldRequestServiceToDeleteAndRedirectToCoursesWithMessage()
            throws Exception {

        long id = 1L;
        Course course = mock(Course.class);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.of(course));

        mvc.perform(
                get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getCourse(id);
        then(timetableFacade).should().deleteCourse(course);
    }

    @Test
    public void postNewShouldValidateFormAndRedirectToCoursesWithErrorMessageIfInvalid()
            throws Exception {

        NewItemForm newItemForm = new NewItemForm();
        newItemForm.setName(" ");

        RequestBuilder requestBuilder =
                post(baseUrl + "/new").flashAttr("newItemForm", newItemForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable(
                (BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent()
                .containsInstanceOf(BindException.class);
    }

    @Test
    public void postNewShouldCreateCourseRequestServiceToSaveAndRedirectToCoursesWithMessage()
            throws Exception {

        Course savedCourse = mock(Course.class);
        long id = 1L;
        String name = "name";
        given(savedCourse.getId()).willReturn(id);
        given(savedCourse.getName()).willReturn(name);
        given(timetableFacade.saveCourse(any(Course.class))).willReturn(
                savedCourse);

        Course newCourse = new Course(name);

        NewItemForm form = new NewItemForm();
        form.setName(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newItemForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().saveCourse(newCourse);
    }

}