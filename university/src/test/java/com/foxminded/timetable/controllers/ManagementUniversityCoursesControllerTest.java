package com.foxminded.timetable.controllers;

import com.foxminded.timetable.forms.NewItemForm;
import com.foxminded.timetable.forms.RenameForm;
import com.foxminded.timetable.forms.ScheduleForm;
import com.foxminded.timetable.forms.ScheduleOption;
import com.foxminded.timetable.forms.utility.DaySchedule;
import com.foxminded.timetable.forms.utility.MonthSchedule;
import com.foxminded.timetable.forms.utility.WeekSchedule;
import com.foxminded.timetable.forms.utility.formatter.ScheduleFormatter;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicateCourseId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    public void postScheduleShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent()
            throws Exception {

        ScheduleForm form = mock(ScheduleForm.class);
        long id = 1L;
        given(form.getId()).willReturn(id);
        given(timetableFacade.getCourse(anyLong())).willReturn(
                Optional.empty());

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", form))
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

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        boolean filtered = true;
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.DAY);
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
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

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        boolean filtered = true;
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getScheduleOption()).willReturn(ScheduleOption.WEEK);
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
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

        ScheduleForm scheduleForm = mock(ScheduleForm.class);
        boolean filtered = true;
        LocalDate date = LocalDate.MAX;
        given(scheduleForm.getScheduleOption()).willReturn(
                ScheduleOption.MONTH);
        given(scheduleForm.getLocalDate()).willReturn(date);
        given(scheduleForm.getId()).willReturn(id);
        given(scheduleForm.isFiltered()).willReturn(filtered);
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
    public void postRenameShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent()
            throws Exception {

        RenameForm form = mock(RenameForm.class);
        long id = 1L;
        given(form.getRenameId()).willReturn(id);
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

        RenameForm form = mock(RenameForm.class);
        given(form.getRenameId()).willReturn(id);
        given(form.getNewName()).willReturn(name);

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

        NewItemForm form = mock(NewItemForm.class);
        given(form.getName()).willReturn(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newItemForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().saveCourse(newCourse);
    }

}