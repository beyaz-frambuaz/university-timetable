package com.shablii.timetable.mvc;

import com.shablii.timetable.config.ControllersTestConfig;
import com.shablii.timetable.forms.*;
import com.shablii.timetable.forms.utility.*;
import com.shablii.timetable.forms.utility.formatter.ScheduleFormatter;
import com.shablii.timetable.model.*;
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
@WebMvcTest(ManagementUniversityFacultyController.class)
class ManagementUniversityFacultyControllerTest {

    private final String baseUrl = "/timetable/management/university/faculty";
    private final String baseView = "management/university/faculty";

    @Autowired
    private MockMvc mvc;
    @MockBean
    private ScheduleFormatter scheduleFormatter;
    @MockBean
    private TimetableFacade timetableFacade;

    @Test
    public void getFacultyShouldRequestFromServiceAndDisplay() throws Exception {

        List<Professor> professors = Collections.emptyList();
        given(timetableFacade.getProfessors()).willReturn(professors);

        mvc.perform(get(baseUrl))
                .andExpect(status().isOk())
                .andExpect(model().attribute("professors", professors))
                .andExpect(model().attributeExists("errorAlert", "successAlert", "editedId", "newProfessorForm"))
                .andExpect(view().name(baseView + "/professors"));

        then(timetableFacade).should().getProfessors();
    }

    @Test
    public void postScheduleShouldValidateFormAndRedirectToFacultyWithErrorMessageIfInvalid() throws Exception {

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
    public void postScheduleShouldRequestProfessorFromServiceAndRedirectToFacultyIfNotPresent() throws Exception {

        long id = 1L;
        ScheduleForm form = new ScheduleForm();
        form.setId(id);
        form.setDate("2020-06-01");
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayDaySchedulePerFormRequest() throws Exception {

        long id = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.DAY);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);

        DaySchedule daySchedule = mock(DaySchedule.class);
        given(scheduleFormatter.prepareDaySchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(daySchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("daySchedule", daySchedule))
                .andExpect(model().attribute("professor", professor))
                .andExpect(view().name(baseView + "/schedule/day"));

        then(scheduleFormatter).should().prepareDaySchedule(predicate, date, filtered);
        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayWeekSchedulePerFormRequest() throws Exception {

        long id = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.WEEK);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);

        WeekSchedule weekSchedule = mock(WeekSchedule.class);
        given(scheduleFormatter.prepareWeekSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(weekSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("weekSchedule", weekSchedule))
                .andExpect(model().attribute("professor", professor))
                .andExpect(view().name(baseView + "/schedule/week"));

        then(scheduleFormatter).should().prepareWeekSchedule(predicate, date, filtered);
        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void postScheduleShouldRequestAndDisplayMonthSchedulePerFormRequest() throws Exception {

        long id = 1L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        boolean filtered = true;
        LocalDate date = LocalDate.MAX;

        ScheduleForm scheduleForm = new ScheduleForm();
        scheduleForm.setDate(date.toString());
        scheduleForm.setId(id);
        scheduleForm.setScheduleOption(ScheduleOption.MONTH);
        scheduleForm.setFiltered(filtered);

        SchedulePredicate predicate = new SchedulePredicateProfessorId(id);

        MonthSchedule monthSchedule = mock(MonthSchedule.class);
        given(scheduleFormatter.prepareMonthSchedule(any(SchedulePredicate.class), any(LocalDate.class),
                anyBoolean())).willReturn(monthSchedule);

        mvc.perform(post(baseUrl + "/schedule").flashAttr("scheduleForm", scheduleForm))
                .andExpect(status().isOk())
                .andExpect(model().attribute("monthSchedule", monthSchedule))
                .andExpect(model().attribute("professor", professor))
                .andExpect(view().name(baseView + "/schedule/month"));

        then(scheduleFormatter).should().prepareMonthSchedule(predicate, date, filtered);
        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void getScheduleShouldRedirectToFaculty() throws Exception {

        mvc.perform(get(baseUrl + "/schedule"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(baseUrl));
    }

    @Test
    public void getRemoveShouldValidateIdAndRedirectToFacultyIfInvalid() throws Exception {

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
    public void getRemoveShouldRequestProfessorFromServiceAndRedirectToFacultyIfNotPresent() throws Exception {

        long id = 1L;
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.empty());

        mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void getRemoveShouldRequestServiceToDeleteAndRedirectToProfessorsWithMessage() throws Exception {

        long id = 1L;
        Professor professor = mock(Professor.class);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        mvc.perform(get(baseUrl + "/remove").queryParam("id", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getProfessor(id);
        then(timetableFacade).should().deleteProfessor(professor);
    }

    @Test
    public void postNewShouldValidateFormAndRedirectToFacultyWithErrorMessageIfInvalid() throws Exception {

        NewProfessorForm newProfessorForm = new NewProfessorForm();
        newProfessorForm.setFirstName(" ");
        newProfessorForm.setLastName(" ");

        RequestBuilder requestBuilder = post(baseUrl + "/new").flashAttr("newProfessorForm", newProfessorForm);
        MvcResult mvcResult = mvc.perform(requestBuilder)
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postNewShouldCreateProfessorRequestServiceToSaveAndRedirectToFacultyWithMessage() throws Exception {

        Professor savedProfessor = mock(Professor.class);
        long id = 1L;
        String name = "name";
        given(savedProfessor.getId()).willReturn(id);
        given(savedProfessor.getFullName()).willReturn(name);
        given(timetableFacade.saveProfessor(any(Professor.class))).willReturn(savedProfessor);

        Professor newProfessor = new Professor(name, name);

        NewProfessorForm form = new NewProfessorForm();
        form.setFirstName(name);
        form.setLastName(name);

        mvc.perform(post(baseUrl + "/new").flashAttr("newProfessorForm", form))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(flash().attribute("editedId", id))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().saveProfessor(newProfessor);
    }

    @Test
    public void getCoursesShouldValidateIdAndRedirectToFacultyIfInvalid() throws Exception {

        long professorId = 0L;

        MvcResult mvcResult = mvc.perform(
                get(baseUrl + "/courses").queryParam("professorId", String.valueOf(professorId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<ConstraintViolationException> exception = Optional.ofNullable(
                (ConstraintViolationException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void getCoursesShouldRequestProfessorFromServiceAndRedirectToFacultyIfNotPresent() throws Exception {

        long id = 1L;
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.empty());

        mvc.perform(get(baseUrl + "/courses").queryParam("professorId", String.valueOf(id)))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void getCoursesShouldRequestFromServiceAndDisplayCourseAttendeesForEachProfessorCourseAndPrepareForms()
            throws Exception {

        long id = 1L;
        Professor professor = mock(Professor.class);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        Course professorCourse = mock(Course.class);
        given(professor.getCourses()).willReturn(Collections.singleton(professorCourse));

        List<Student> students = Collections.emptyList();
        given(timetableFacade.getCourseAttendees(any(Course.class), any(Professor.class))).willReturn(students);
        Map<Course, List<Student>> allCourseAttendees = new HashMap<>();
        allCourseAttendees.put(professorCourse, students);

        Course otherCourse = mock(Course.class);
        List<Course> allCourses = new ArrayList<>();
        allCourses.add(professorCourse);
        allCourses.add(otherCourse);
        given(timetableFacade.getCourses()).willReturn(allCourses);
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setNewCourses(Collections.singletonList(otherCourse));

        mvc.perform(get(baseUrl + "/courses").queryParam("professorId", String.valueOf(id)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("errorAlert", "successAlert", "editedId", "dropCourseForm"))
                .andExpect(model().attribute("professor", professor))
                .andExpect(model().attribute("allCourseAttendees", allCourseAttendees))
                .andExpect(model().attribute("addCourseForm", addCourseForm))
                .andExpect(view().name(baseView + "/courses"));

        then(timetableFacade).should().getProfessor(id);
        then(timetableFacade).should().getCourseAttendees(professorCourse, professor);
        then(timetableFacade).should().getCourses();
    }

    @Test
    public void postCoursesAddShouldValidateFormAndRedirectToFacultyIfInvalid() throws Exception {

        long courseId = 0L;
        long professorId = 0L;
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setNewCourse(courseId);
        addCourseForm.setProfessorId(professorId);

        MvcResult mvcResult = mvc.perform(post(baseUrl + "/courses/add").flashAttr("addCourseForm", addCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postCoursesAddShouldRequestProfessorFromServiceAndRedirectToFacultyIfNotPresent() throws Exception {

        long id = 1L;
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setProfessorId(id);
        addCourseForm.setNewCourse(id);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/courses/add").flashAttr("addCourseForm", addCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void postCoursesAddShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent() throws Exception {

        long professorId = 1L;
        long courseId = 2L;
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setProfessorId(professorId);
        addCourseForm.setNewCourse(courseId);

        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(professorId);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));
        given(timetableFacade.getCourse(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/courses/add").flashAttr("addCourseForm", addCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl + "/courses?professorId=" + professorId));

        then(timetableFacade).should().getProfessor(professorId);
        then(timetableFacade).should().getCourse(courseId);
    }

    @Test
    public void postCoursesAddShouldAddCourseToProfessorRequestServiceToSaveAndRedirectToCoursesWithMessage()
            throws Exception {

        long professorId = 1L;
        long courseId = 2L;
        AddCourseForm addCourseForm = new AddCourseForm();
        addCourseForm.setProfessorId(professorId);
        addCourseForm.setNewCourse(courseId);

        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(professorId);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        Course course = mock(Course.class);
        given(course.getId()).willReturn(courseId);
        given(course.getName()).willReturn("");
        given(timetableFacade.getCourse(anyLong())).willReturn(Optional.of(course));

        mvc.perform(post(baseUrl + "/courses/add").flashAttr("addCourseForm", addCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert", "editedId"))
                .andExpect(redirectedUrl(baseUrl + "/courses?professorId=" + professorId));

        then(timetableFacade).should().getProfessor(professorId);
        then(timetableFacade).should().getCourse(courseId);
        then(professor).should().addCourse(course);
        then(timetableFacade).should().saveProfessor(professor);
    }

    @Test
    public void postCoursesDropShouldValidateFormAndRedirectToFacultyIfInvalid() throws Exception {

        long courseId = 0L;
        long professorId = 0L;
        DropCourseForm dropCourseForm = new DropCourseForm();
        dropCourseForm.setCourseId(courseId);
        dropCourseForm.setProfessorId(professorId);

        MvcResult mvcResult = mvc.perform(post(baseUrl + "/courses/add").flashAttr("dropCourseForm", dropCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl))
                .andReturn();

        Optional<BindException> exception = Optional.ofNullable((BindException) mvcResult.getResolvedException());

        assertThat(exception).isPresent().containsInstanceOf(BindException.class);
    }

    @Test
    public void postCoursesDropShouldRequestProfessorFromServiceAndRedirectToFacultyIfNotPresent() throws Exception {

        long id = 1L;
        DropCourseForm dropCourseForm = new DropCourseForm();
        dropCourseForm.setProfessorId(id);
        dropCourseForm.setCourseId(id);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/courses/drop").flashAttr("dropCourseForm", dropCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl));

        then(timetableFacade).should().getProfessor(id);
    }

    @Test
    public void postCoursesDropShouldRequestCourseFromServiceAndRedirectToCoursesIfNotPresent() throws Exception {

        long professorId = 1L;
        long courseId = 2L;
        DropCourseForm dropCourseForm = new DropCourseForm();
        dropCourseForm.setProfessorId(professorId);
        dropCourseForm.setCourseId(courseId);

        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(professorId);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));
        given(timetableFacade.getCourse(anyLong())).willReturn(Optional.empty());

        mvc.perform(post(baseUrl + "/courses/drop").flashAttr("dropCourseForm", dropCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorAlert"))
                .andExpect(redirectedUrl(baseUrl + "/courses?professorId=" + professorId));

        then(timetableFacade).should().getProfessor(professorId);
        then(timetableFacade).should().getCourse(courseId);
    }

    @Test
    public void postCoursesDropShouldAddCourseToProfessorRequestServiceToSaveAndRedirectToCoursesWithMessage()
            throws Exception {

        long professorId = 1L;
        long courseId = 2L;
        DropCourseForm dropCourseForm = new DropCourseForm();
        dropCourseForm.setProfessorId(professorId);
        dropCourseForm.setCourseId(courseId);

        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(professorId);
        given(timetableFacade.getProfessor(anyLong())).willReturn(Optional.of(professor));

        Course course = mock(Course.class);
        given(course.getId()).willReturn(courseId);
        given(course.getName()).willReturn("");
        given(timetableFacade.getCourse(anyLong())).willReturn(Optional.of(course));

        mvc.perform(post(baseUrl + "/courses/drop").flashAttr("dropCourseForm", dropCourseForm))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("successAlert"))
                .andExpect(redirectedUrl(baseUrl + "/courses?professorId=" + professorId));

        then(timetableFacade).should().getProfessor(professorId);
        then(timetableFacade).should().getCourse(courseId);
        then(professor).should().removeCourse(course);
        then(timetableFacade).should().saveProfessor(professor);
    }

}