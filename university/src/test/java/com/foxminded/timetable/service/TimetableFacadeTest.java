package com.foxminded.timetable.service;

import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.validation.ConstraintViolationException;
import java.time.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
public class TimetableFacadeTest {

    private final long id = 1L;
    private final Auditorium auditorium = new Auditorium(id, "test");
    private final Course course = new Course(id, "test");
    private final Group group = new Group(id, "test");
    private final Professor professor = new Professor("test", "test");
    private final Student student = new Student("test", "test");
    private final ReschedulingOption option =
            new ReschedulingOption(id, DayOfWeek.MONDAY, Period.FIRST,
                    auditorium);
    private final ScheduleTemplate template =
            new ScheduleTemplate(id, false, DayOfWeek.MONDAY, Period.FIRST,
                    auditorium, course, group, professor);
    private final Schedule schedule = new Schedule(template, LocalDate.MAX);

    private final long invalidId = -1L;
    private final Auditorium invalidAuditorium = new Auditorium(invalidId, " ");
    private final Course invalidCourse = new Course(invalidId, " ");
    private final Group invalidGroup = new Group(invalidId, " ");
    private final Professor invalidProfessor =
            new Professor(invalidId, " ", " ");
    private final Student invalidStudent =
            new Student(invalidId, " ", " ", null);
    private final ReschedulingOption invalidOption =
            new ReschedulingOption(invalidId, null, null, null);
    private final ScheduleTemplate invalidTemplate =
            new ScheduleTemplate(invalidId, false, null, null,
                    invalidAuditorium, invalidCourse, invalidGroup,
                    invalidProfessor);
    private final Schedule invalidSchedule =
            new Schedule(invalidId, invalidTemplate, null, null, null,
                    invalidAuditorium, invalidCourse, invalidGroup,
                    invalidProfessor);

    @MockBean
    private SemesterCalendar semesterCalendar;
    @MockBean
    private AuditoriumService auditoriumService;
    @MockBean
    private ProfessorService professorService;
    @MockBean
    private GroupService groupService;
    @MockBean
    private CourseService courseService;
    @MockBean
    private StudentService studentService;
    @MockBean
    private ScheduleTemplateService templateService;
    @MockBean
    private ScheduleService scheduleService;
    @MockBean
    private ReschedulingOptionService optionService;

    @Autowired
    private TimetableFacade timetableFacade;

    @Test
    public void countAuditoriumsShouldDelegateToAuditoriumService() {

        long expected = 1L;
        given(auditoriumService.count()).willReturn(expected);

        long actual = timetableFacade.countAuditoriums();

        then(auditoriumService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAuditoriumShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveAuditorium(invalidAuditorium));
    }

    @Test
    public void saveAuditoriumShouldDelegateToAuditoriumService() {

        given(auditoriumService.save(any(Auditorium.class))).willReturn(
                auditorium);

        Auditorium actual = timetableFacade.saveAuditorium(auditorium);

        then(auditoriumService).should().save(auditorium);
        assertThat(actual).isEqualTo(auditorium);
    }

    @Test
    public void saveAuditoriumsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveAuditoriums(
                        Collections.singletonList(invalidAuditorium)));
    }

    @Test
    public void saveAuditoriumsShouldDelegateToAuditoriumService() {

        List<Auditorium> expected = Collections.singletonList(auditorium);
        given(auditoriumService.saveAll(anyList())).willReturn(expected);

        List<Auditorium> actual = timetableFacade.saveAuditoriums(expected);

        then(auditoriumService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAuditoriumShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getAuditorium(invalidId));
    }

    @Test
    public void getAuditoriumShouldDelegateToAuditoriumService() {

        Optional<Auditorium> expected = Optional.of(auditorium);
        given(auditoriumService.findById(anyLong())).willReturn(expected);
        Optional<Auditorium> actual = timetableFacade.getAuditorium(id);

        then(auditoriumService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAuditoriumsShouldDelegateToAuditoriumService() {

        List<Auditorium> expected = Collections.singletonList(auditorium);
        given(auditoriumService.findAll()).willReturn(expected);

        List<Auditorium> actual = timetableFacade.getAuditoriums();

        then(auditoriumService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAvailableAuditoriumsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getAvailableAuditoriums(null, null));
    }

    @Test
    public void getAvailableAuditoriumsShouldDelegateToAuditoriumService() {

        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        List<Auditorium> expected = Collections.singletonList(auditorium);
        given(auditoriumService.findAvailableFor(any(LocalDate.class),
                any(Period.class))).willReturn(expected);

        List<Auditorium> actual =
                timetableFacade.getAvailableAuditoriums(date, period);

        then(auditoriumService).should().findAvailableFor(date, period);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteAuditoriumShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.deleteAuditorium(invalidAuditorium));
    }

    @Test
    public void deleteAuditoriumShouldDelegateToAuditoriumService() {

        timetableFacade.deleteAuditorium(auditorium);

        then(auditoriumService).should().delete(auditorium);
    }

    @Test
    public void deleteAllAuditoriumsShouldDelegateToAuditoriumService() {

        timetableFacade.deleteAllAuditoriums();

        then(auditoriumService).should().deleteAll();
    }

    @Test
    public void countCoursesShouldDelegateToCourseService() {

        long expected = 1L;
        given(courseService.count()).willReturn(expected);

        long actual = timetableFacade.countCourses();

        then(courseService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveCourseShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveCourse(invalidCourse));
    }

    @Test
    public void saveCourseShouldDelegateToCourseService() {

        given(courseService.save(any(Course.class))).willReturn(course);

        Course actual = timetableFacade.saveCourse(course);

        then(courseService).should().save(course);
        assertThat(actual).isEqualTo(course);
    }

    @Test
    public void saveCoursesShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveCourses(
                        Collections.singletonList(invalidCourse)));
    }

    @Test
    public void saveCoursesShouldDelegateToCourseService() {

        List<Course> expected = Collections.singletonList(course);
        given(courseService.saveAll(anyList())).willReturn(expected);

        List<Course> actual = timetableFacade.saveCourses(expected);

        then(courseService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getCourseShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getCourse(invalidId));
    }

    @Test
    public void getCourseShouldDelegateToCourseService() {

        Optional<Course> expected = Optional.of(course);
        given(courseService.findById(anyLong())).willReturn(expected);
        Optional<Course> actual = timetableFacade.getCourse(id);

        then(courseService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getCoursesShouldDelegateToCourseService() {

        List<Course> expected = Collections.singletonList(course);
        given(courseService.findAll()).willReturn(expected);

        List<Course> actual = timetableFacade.getCourses();

        then(courseService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteCourseShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.deleteCourse(invalidCourse));
    }

    @Test
    public void deleteCourseShouldDelegateToCourseService() {

        timetableFacade.deleteCourse(course);

        then(courseService).should().delete(course);
    }

    @Test
    public void deleteAllCoursesShouldDelegateToCourseService() {

        timetableFacade.deleteAllCourses();

        then(courseService).should().deleteAll();
    }

    @Test
    public void countGroupsShouldDelegateToGroupService() {

        long expected = 1L;
        given(groupService.count()).willReturn(expected);

        long actual = timetableFacade.countGroups();

        then(groupService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveGroupShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveGroup(invalidGroup));
    }

    @Test
    public void saveGroupShouldDelegateToGroupService() {

        given(groupService.save(any(Group.class))).willReturn(group);

        Group actual = timetableFacade.saveGroup(group);

        then(groupService).should().save(group);
        assertThat(actual).isEqualTo(group);
    }

    @Test
    public void saveGroupsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveGroups(
                        Collections.singletonList(invalidGroup)));
    }

    @Test
    public void saveGroupsShouldDelegateToGroupService() {

        List<Group> expected = Collections.singletonList(group);
        given(groupService.saveAll(anyList())).willReturn(expected);

        List<Group> actual = timetableFacade.saveGroups(expected);

        then(groupService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getGroupShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getGroup(invalidId));
    }

    @Test
    public void getGroupShouldDelegateToGroupService() {

        Optional<Group> expected = Optional.of(group);
        given(groupService.findById(anyLong())).willReturn(expected);

        Optional<Group> actual = timetableFacade.getGroup(id);

        then(groupService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getGroupsShouldDelegateToGroupService() {

        List<Group> expected = Collections.singletonList(group);
        given(groupService.findAll()).willReturn(expected);

        List<Group> actual = timetableFacade.getGroups();

        then(groupService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getGroupedStudentsShouldRequestAllGroupsAndStudentsAndGroupThem() {

        Group groupOne = new Group(1L, "groupOne");
        Group groupTwo = new Group(2L, "groupTwo");
        Student studentOne = new Student(1L, "student", "one", groupOne);
        Student studentTwo = new Student(2L, "student", "two", groupOne);

        given(groupService.findAll()).willReturn(
                Arrays.asList(groupOne, groupTwo));
        given(studentService.findAll()).willReturn(
                Arrays.asList(studentOne, studentTwo));

        Map<Group, List<Student>> expected = new LinkedHashMap<>();
        expected.put(groupOne, Arrays.asList(studentOne, studentTwo));
        expected.put(groupTwo, Collections.emptyList());

        Map<Group, List<Student>> actual = timetableFacade.getGroupedStudents();

        assertThat(actual).isEqualTo(expected);
        then(groupService).should().findAll();
        then(studentService).should().findAll();
    }

    @Test
    public void deleteGroupShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.deleteGroup(invalidGroup));
    }

    @Test
    public void deleteGroupShouldDelegateToGroupService() {

        timetableFacade.deleteGroup(group);

        then(groupService).should().delete(group);
    }

    @Test
    public void deleteAllGroupsShouldDelegateToGroupService() {

        timetableFacade.deleteAllGroups();

        then(groupService).should().deleteAll();
    }

    @Test
    public void countProfessorsShouldDelegateToProfessorService() {

        long expected = 1L;
        given(professorService.count()).willReturn(expected);

        long actual = timetableFacade.countProfessors();

        then(professorService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveProfessorShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveProfessor(invalidProfessor));
    }

    @Test
    public void saveProfessorShouldDelegateToProfessorService() {

        given(professorService.save(any(Professor.class))).willReturn(
                professor);

        Professor actual = timetableFacade.saveProfessor(professor);

        then(professorService).should().save(professor);
        assertThat(actual).isEqualTo(professor);
    }

    @Test
    public void saveProfessorsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveProfessors(
                        Collections.singletonList(invalidProfessor)));
    }

    @Test
    public void saveProfessorsShouldDelegateToProfessorService() {

        List<Professor> expected = Collections.singletonList(professor);
        given(professorService.saveAll(anyList())).willReturn(expected);

        List<Professor> actual = timetableFacade.saveProfessors(expected);

        then(professorService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getProfessorShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getProfessor(invalidId));
    }

    @Test
    public void getProfessorShouldDelegateToProfessorService() {

        Optional<Professor> expected = Optional.of(professor);
        given(professorService.findById(anyLong())).willReturn(expected);

        Optional<Professor> actual = timetableFacade.getProfessor(id);

        then(professorService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getProfessorsShouldDelegateToProfessorService() {

        List<Professor> expected = Collections.singletonList(professor);
        given(professorService.findAll()).willReturn(expected);

        List<Professor> actual = timetableFacade.getProfessors();

        then(professorService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getProfessorsTeachingShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getProfessorsTeaching(invalidCourse));
    }

    @Test
    public void getAvailableProfessorsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getAvailableProfessors(null, null));
    }

    @Test
    public void getAvailableProfessorsShouldDelegateToProfessorService() {

        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        List<Professor> expected = Collections.singletonList(professor);
        given(professorService.findAvailableFor(any(LocalDate.class),
                any(Period.class))).willReturn(expected);

        List<Professor> actual =
                timetableFacade.getAvailableProfessors(date, period);

        then(professorService).should().findAvailableFor(date, period);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteProfessorShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.deleteProfessor(invalidProfessor));
    }

    @Test
    public void deleteProfessorShouldDelegateToProfessorService() {

        timetableFacade.deleteProfessor(professor);

        then(professorService).should().delete(professor);
    }

    @Test
    public void deleteAllProfessorsShouldDelegateToProfessorService() {

        timetableFacade.deleteAllProfessors();

        then(professorService).should().deleteAll();
    }

    @Test
    public void countOptionsShouldDelegateToOptionService() {

        long expected = 1L;
        given(optionService.count()).willReturn(expected);

        long actual = timetableFacade.countOptions();

        then(optionService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveOptionsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveOptions(
                        Collections.singletonList(invalidOption)));
    }

    @Test
    public void saveOptionsShouldDelegateToOptionService() {

        List<ReschedulingOption> expected = Collections.singletonList(option);
        given(optionService.saveAll(anyList())).willReturn(expected);

        List<ReschedulingOption> actual = timetableFacade.saveOptions(expected);

        then(optionService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getOption(invalidId));
    }

    @Test
    public void getOptionShouldDelegateToOptionService() {

        Optional<ReschedulingOption> expected = Optional.of(option);
        given(optionService.findById(anyLong())).willReturn(expected);

        Optional<ReschedulingOption> actual = timetableFacade.getOption(id);

        then(optionService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionsShouldDelegateToOptionService() {

        List<ReschedulingOption> expected = Collections.singletonList(option);
        given(optionService.findAll()).willReturn(expected);

        List<ReschedulingOption> actual = timetableFacade.getOptions();

        then(optionService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionsForWeekShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getOptionsForWeek(invalidSchedule, 1));
    }

    @Test
    public void getOptionsForWeekShouldDelegateAppropriateCallsToUnderlyingServices() {

        int week = 1;
        LocalDate monday = LocalDate.MIN;
        LocalDate friday = LocalDate.MAX;
        given(semesterCalendar.isSemesterWeek(anyInt())).willReturn(true);
        given(semesterCalendar.getWeekMonday(anyInt())).willReturn(monday);
        given(semesterCalendar.getWeekFriday(anyInt())).willReturn(friday);

        List<ScheduleTemplate> templates = Collections.emptyList();
        given(templateService.findAllForWeek(anyBoolean())).willReturn(
                templates);

        List<Schedule> schedules = Collections.emptyList();
        given(scheduleService.findGeneratedInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(schedules);

        List<ReschedulingOption> options = Collections.emptyList();
        given(optionService.findAll()).willReturn(options);

        timetableFacade.getOptionsForWeek(schedule, week);

        then(templateService).should().findAllForWeek(false);
        then(scheduleService).should().findGeneratedInRange(monday, friday);
        then(optionService).should().findAll();
    }

    @Test
    public void getOptionsForWeekShouldReturnEmptyListGivenNonSemesterWeek() {

        int week = 1;
        given(semesterCalendar.isSemesterWeek(anyInt())).willReturn(false);

        List<ReschedulingOption> actual =
                timetableFacade.getOptionsForWeek(schedule, week);

        assertThat(actual).isEmpty();
    }

    @Test
    public void getOptionsForDateShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getOptionsForDate(invalidSchedule, null));
    }

    @Test
    public void getOptionsForDateShouldDelegateAppropriateCallsToUnderlyingServices() {

        LocalDate date = LocalDate.MAX;
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(true);

        List<ScheduleTemplate> templates = Collections.emptyList();
        given(templateService.findAllForDay(anyBoolean(), any(DayOfWeek.class)))
                .willReturn(templates);

        List<Schedule> schedules = Collections.emptyList();
        given(scheduleService.findGeneratedInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(schedules);

        List<ReschedulingOption> options = Collections.emptyList();
        given(optionService.findAllForDay(any(DayOfWeek.class))).willReturn(
                options);

        timetableFacade.getOptionsForDate(schedule, date);

        then(templateService).should().findAllForDay(true, date.getDayOfWeek());
        then(scheduleService).should().findGeneratedInRange(date, date);
        then(optionService).should().findAllForDay(date.getDayOfWeek());
    }

    @Test
    public void getOptionsForDateShouldFilterOutOptionsWithBusyAuditoriums() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(true);

        LocalDate date = LocalDate.of(2020, 6, 1);
        DayOfWeek day = DayOfWeek.MONDAY;
        Period period = Period.FIRST;
        Auditorium availableAuditorium = new Auditorium(1L, "available");
        Auditorium busyAuditoriumOne = new Auditorium(2L, "busy 1");
        Auditorium busyAuditoriumTwo = new Auditorium(3L, "busy 2");

        ReschedulingOption availableOption =
                new ReschedulingOption(1L, day, period, availableAuditorium);
        ReschedulingOption busyOptionOne =
                new ReschedulingOption(2L, day, period, busyAuditoriumOne);
        ReschedulingOption busyOptionTwo =
                new ReschedulingOption(3L, day, period, busyAuditoriumTwo);
        List<ReschedulingOption> options =
                Arrays.asList(availableOption, busyOptionOne, busyOptionTwo);

        given(optionService.findAllForDay(any(DayOfWeek.class))).willReturn(
                options);

        Schedule generatedSchedule = mock(Schedule.class);
        given(generatedSchedule.getDay()).willReturn(day);
        given(generatedSchedule.getPeriod()).willReturn(period);
        given(generatedSchedule.getAuditorium()).willReturn(busyAuditoriumOne);
        given(generatedSchedule.getGroup()).willReturn(group);
        given(generatedSchedule.getProfessor()).willReturn(professor);
        given(scheduleService.findGeneratedInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(
                Collections.singletonList(generatedSchedule));

        ScheduleTemplate template = mock(ScheduleTemplate.class);
        given(template.getDay()).willReturn(day);
        given(template.getPeriod()).willReturn(period);
        given(template.getAuditorium()).willReturn(busyAuditoriumTwo);
        given(template.getGroup()).willReturn(group);
        given(template.getProfessor()).willReturn(professor);
        given(templateService.findAllForDay(anyBoolean(), any(DayOfWeek.class)))
                .willReturn(Collections.singletonList(template));

        Schedule candidate = new Schedule(schedule);
        candidate.setProfessor(new Professor("available", "professor"));
        candidate.setGroup(new Group("available"));

        List<ReschedulingOption> actual =
                timetableFacade.getOptionsForDate(candidate, date);

        assertThat(actual).containsOnly(availableOption)
                .doesNotContain(busyOptionOne, busyOptionTwo);
    }

    @Test
    public void getOptionsForDateShouldFilterOutOptionsInPeriodsWithBusyGroupOrProfessor() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(true);

        LocalDate date = LocalDate.of(2020, 6, 1);
        DayOfWeek day = DayOfWeek.MONDAY;
        Period availablePeriod = Period.FIRST;
        Period busyPeriodOne = Period.SECOND;
        Period busyPeriodTwo = Period.THIRD;
        Period busyPeriodThree = Period.FOURTH;
        Period busyPeriodFour = Period.FIFTH;
        Auditorium auditorium = mock(Auditorium.class);
        Auditorium anotherAuditorium = mock(Auditorium.class);


        ReschedulingOption availableOption =
                new ReschedulingOption(1L, day, availablePeriod, auditorium);
        ReschedulingOption busyOptionOne =
                new ReschedulingOption(2L, day, busyPeriodOne, auditorium);
        ReschedulingOption busyOptionTwo =
                new ReschedulingOption(3L, day, busyPeriodTwo, auditorium);
        ReschedulingOption busyOptionThree =
                new ReschedulingOption(4L, day, busyPeriodThree, auditorium);
        ReschedulingOption busyOptionFour =
                new ReschedulingOption(5L, day, busyPeriodFour, auditorium);
        List<ReschedulingOption> options =
                Arrays.asList(availableOption, busyOptionOne, busyOptionTwo,
                        busyOptionThree, busyOptionFour);

        given(optionService.findAllForDay(any(DayOfWeek.class))).willReturn(
                options);

        Schedule scheduleOne = mock(Schedule.class);
        given(scheduleOne.getDay()).willReturn(day);
        given(scheduleOne.getPeriod()).willReturn(busyPeriodOne);
        given(scheduleOne.getAuditorium()).willReturn(anotherAuditorium);
        given(scheduleOne.getGroup()).willReturn(group);

        Schedule scheduleTwo = mock(Schedule.class);
        given(scheduleTwo.getDay()).willReturn(day);
        given(scheduleTwo.getPeriod()).willReturn(busyPeriodTwo);
        given(scheduleTwo.getAuditorium()).willReturn(anotherAuditorium);
        given(scheduleTwo.getGroup()).willReturn(mock(Group.class));
        given(scheduleTwo.getProfessor()).willReturn(professor);

        given(scheduleService.findGeneratedInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(
                Arrays.asList(scheduleOne, scheduleTwo));

        ScheduleTemplate templateOne = mock(ScheduleTemplate.class);
        given(templateOne.getDay()).willReturn(day);
        given(templateOne.getPeriod()).willReturn(busyPeriodThree);
        given(templateOne.getAuditorium()).willReturn(anotherAuditorium);
        given(templateOne.getGroup()).willReturn(group);

        ScheduleTemplate templateTwo = mock(ScheduleTemplate.class);
        given(templateTwo.getDay()).willReturn(day);
        given(templateTwo.getPeriod()).willReturn(busyPeriodFour);
        given(templateTwo.getAuditorium()).willReturn(anotherAuditorium);
        given(templateTwo.getGroup()).willReturn(mock(Group.class));
        given(templateTwo.getProfessor()).willReturn(professor);

        given(templateService.findAllForDay(anyBoolean(), any(DayOfWeek.class)))
                .willReturn(Arrays.asList(templateOne, templateTwo));

        Schedule candidate = mock(Schedule.class);
        given(candidate.getProfessor()).willReturn(professor);
        given(candidate.getGroup()).willReturn(group);

        List<ReschedulingOption> actual =
                timetableFacade.getOptionsForDate(schedule, date);

        assertThat(actual).containsOnly(availableOption)
                .doesNotContain(busyOptionOne, busyOptionTwo, busyOptionThree,
                        busyOptionFour);
    }

    @Test
    public void getOptionsForDateShouldReturnEmptyListGivenNonSemesterDate() {

        LocalDate date = LocalDate.MAX;
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                false);

        List<ReschedulingOption> actual =
                timetableFacade.getOptionsForDate(schedule, date);

        assertThat(actual).isEmpty();
    }

    @Test
    public void deleteAllOptionsShouldDelegateToOptionService() {

        timetableFacade.deleteAllOptions();

        then(optionService).should().deleteAll();
    }

    @Test
    public void saveScheduleShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveSchedule(invalidSchedule));
    }

    @Test
    public void saveScheduleShouldDelegateToScheduleService() {

        given(scheduleService.save(any(Schedule.class))).willReturn(schedule);

        Schedule actual = timetableFacade.saveSchedule(schedule);

        then(scheduleService).should().save(schedule);
        assertThat(actual).isEqualTo(schedule);
    }

    @Test
    public void saveSchedulesShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveSchedules(
                        Collections.singletonList(invalidSchedule)));
    }

    @Test
    public void saveSchedulesShouldDelegateToScheduleService() {

        List<Schedule> expected = Collections.singletonList(schedule);
        given(scheduleService.saveAll(anyList())).willReturn(expected);

        List<Schedule> actual = timetableFacade.saveSchedules(expected);

        then(scheduleService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getScheduleShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getSchedule(invalidId));
    }

    @Test
    public void getScheduleShouldDelegateToScheduleService() {

        Optional<Schedule> expected = Optional.of(schedule);
        given(scheduleService.findById(anyLong())).willReturn(expected);

        Optional<Schedule> actual = timetableFacade.getSchedule(id);

        then(scheduleService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getSchedulesShouldDelegateToScheduleService() {

        List<Schedule> expected = Collections.singletonList(schedule);
        given(scheduleService.findAll()).willReturn(expected);

        List<Schedule> actual = timetableFacade.getSchedules();

        then(scheduleService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getScheduleForShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getScheduleFor(null, null, null));
    }

    @Test
    public void getScheduleForShouldDelegateToScheduleService() {

        SchedulePredicate predicate = new SchedulePredicateNoFilter();
        LocalDate date = LocalDate.MAX;
        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.findAllFor(any(SchedulePredicate.class),
                any(LocalDate.class), any(LocalDate.class))).willReturn(
                expected);

        List<Schedule> actual =
                timetableFacade.getScheduleFor(predicate, date, date);

        assertThat(actual).isEqualTo(expected);
        then(scheduleService).should().findAllFor(predicate, date, date);
    }

    @Test
    public void getScheduleInRangeShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getScheduleInRange(null, null));
    }

    @Test
    public void getScheduleInRangeShouldDelegateToScheduleService() {

        LocalDate date = LocalDate.MAX;
        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.findAllInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(expected);

        List<Schedule> actual = timetableFacade.getScheduleInRange(date, date);

        then(scheduleService).should().findAllInRange(date, date);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteAllSchedulesShouldDelegateToScheduleService() {

        timetableFacade.deleteAllSchedules();

        then(scheduleService).should().deleteAll();
    }

    @Test
    public void countTemplatesShouldDelegateToTemplateService() {

        long expected = 1L;
        given(templateService.count()).willReturn(expected);

        long actual = timetableFacade.countTemplates();

        then(templateService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveTemplateShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveTemplate(invalidTemplate));
    }

    @Test
    public void saveTemplateShouldDelegateToTemplateService() {

        given(templateService.save(any(ScheduleTemplate.class))).willReturn(
                template);

        ScheduleTemplate actual = timetableFacade.saveTemplate(template);

        then(templateService).should().save(template);
        assertThat(actual).isEqualTo(template);
    }

    @Test
    public void saveTemplatesShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveTemplates(
                        Collections.singletonList(invalidTemplate)));
    }

    @Test
    public void saveTemplatesShouldDelegateToTemplateService() {

        List<ScheduleTemplate> expected = Collections.singletonList(template);
        given(templateService.saveAll(anyList())).willReturn(expected);

        List<ScheduleTemplate> actual = timetableFacade.saveTemplates(expected);

        then(templateService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getTemplateShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getTemplate(invalidId));
    }

    @Test
    public void getTemplateShouldDelegateToTemplateService() {

        Optional<ScheduleTemplate> expected = Optional.of(template);
        given(templateService.findById(anyLong())).willReturn(expected);

        Optional<ScheduleTemplate> actual = timetableFacade.getTemplate(id);

        then(templateService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getTwoWeekScheduleShouldDelegateToTemplateService() {

        List<ScheduleTemplate> expected = Collections.emptyList();
        given(templateService.findAll()).willReturn(expected);

        List<ScheduleTemplate> actual = timetableFacade.getTwoWeekSchedule();

        then(templateService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteAllTemplatesShouldDelegateToTemplateService() {

        timetableFacade.deleteAllTemplates();

        then(templateService).should().deleteAll();
    }

    @Test
    public void countStudentsShouldDelegateToStudentService() {

        long expected = 1L;
        given(studentService.count()).willReturn(expected);

        long actual = timetableFacade.countStudents();

        then(studentService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveStudentShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveStudent(invalidStudent));
    }

    @Test
    public void saveStudentShouldDelegateToStudentService() {

        given(studentService.save(any(Student.class))).willReturn(student);

        Student actual = timetableFacade.saveStudent(student);

        then(studentService).should().save(student);
        assertThat(actual).isEqualTo(student);
    }

    @Test
    public void saveStudentsShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.saveStudents(
                        Collections.singletonList(invalidStudent)));
    }

    @Test
    public void saveStudentsShouldDelegateToStudentService() {

        List<Student> expected = Collections.singletonList(student);
        given(studentService.saveAll(anyList())).willReturn(expected);

        List<Student> actual = timetableFacade.saveStudents(expected);

        then(studentService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getStudentShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getStudent(invalidId));
    }

    @Test
    public void getStudentShouldDelegateToStudentService() {

        Optional<Student> expected = Optional.of(student);
        given(studentService.findById(anyLong())).willReturn(expected);

        Optional<Student> actual = timetableFacade.getStudent(id);

        then(studentService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getStudentsShouldDelegateToStudentService() {

        List<Student> expected = Collections.emptyList();
        given(studentService.findAll()).willReturn(expected);

        List<Student> actual = timetableFacade.getStudents();

        then(studentService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getCourseAttendeesShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.getCourseAttendees(invalidCourse,
                        invalidProfessor));
    }

    @Test
    public void getCourseAttendeesShouldDelegateToGroupAndStudentServices() {

        List<Group> professorGroups = Collections.emptyList();
        given(groupService.findAllAttendingProfessorCourse(any(Course.class),
                any(Professor.class))).willReturn(professorGroups);
        List<Student> expected = Collections.emptyList();
        given(studentService.findAllInGroups(anyList())).willReturn(expected);

        List<Student> actual =
                timetableFacade.getCourseAttendees(course, professor);

        then(groupService).should()
                .findAllAttendingProfessorCourse(course, professor);
        then(studentService).should().findAllInGroups(professorGroups);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteStudentShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.deleteStudent(invalidStudent));
    }

    @Test
    public void deleteStudentShouldDelegateToStudentService() {

        timetableFacade.deleteStudent(student);

        then(studentService).should().delete(student);
    }

    @Test
    public void deleteAllStudentsShouldDelegateToStudentService() {

        timetableFacade.deleteAllStudents();

        then(studentService).should().deleteAll();
    }

    @Test
    public void substituteProfessorShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.substituteProfessor(invalidSchedule,
                        invalidProfessor));
    }

    @Test
    public void substituteProfessorShouldSetProfessorAndDelegateSaveToService() {

        Schedule expected = new Schedule(schedule);
        Professor substitute = new Professor("new", "professor");
        given(scheduleService.save(any(Schedule.class))).willReturn(expected);

        Schedule actual =
                timetableFacade.substituteProfessor(expected, substitute);

        then(scheduleService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
        assertThat(expected.getProfessor()).isEqualTo(substitute);
    }

    @Test
    public void rescheduleOnceShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.rescheduleSingle(invalidSchedule, null,
                        invalidOption));
    }

    @Test
    public void rescheduleOnceShouldSetNewScheduleAttributesAndDelegateSaveToService() {

        LocalDate date = LocalDate.MIN;
        DayOfWeek day = DayOfWeek.FRIDAY;
        Period period = Period.FIFTH;
        Auditorium newAuditorium = new Auditorium(id, "new");
        ReschedulingOption option =
                new ReschedulingOption(id, day, period, newAuditorium);
        Schedule expected = new Schedule(schedule);

        given(scheduleService.save(any(Schedule.class))).willReturn(expected);

        Schedule actual =
                timetableFacade.rescheduleSingle(expected, date, option);

        then(scheduleService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
        assertThat(expected.getDate()).isEqualTo(date);
        assertThat(expected.getDay()).isEqualTo(day);
        assertThat(expected.getPeriod()).isEqualTo(period);
        assertThat(expected.getAuditorium()).isEqualTo(newAuditorium);
    }

    @Test
    public void reschedulePermanentlyShouldValidate() {

        assertThatExceptionOfType(
                ConstraintViolationException.class).isThrownBy(
                () -> timetableFacade.rescheduleRecurring(invalidSchedule, null,
                        invalidOption));
    }

    /*
     * 1. should request template from service
     * 2. should set template attributes
     * 3. should set schedule attributes
     * 4. should delegate update all schedules with same template id to service
     */
    @Test
    public void reschedulePermanentlyTest() {

        ScheduleTemplate underlyingTemplate =
                new ScheduleTemplate(id, true, DayOfWeek.MONDAY, Period.FIRST,
                        auditorium, course, group, professor);

        Schedule candidate = new Schedule(underlyingTemplate, LocalDate.MAX);

        given(templateService.findById(anyLong())).willReturn(
                Optional.of(underlyingTemplate));

        LocalDate date = LocalDate.MIN;
        DayOfWeek newDay = DayOfWeek.WEDNESDAY;
        Period newPeriod = Period.THIRD;
        Auditorium newAuditorium = new Auditorium(id, "new");
        ReschedulingOption option =
                new ReschedulingOption(id, newDay, newPeriod, newAuditorium);

        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);

        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.updateAllWithSameTemplateId(any(Schedule.class),
                any(LocalDate.class))).willReturn(expected);

        List<Schedule> actual = null;
        actual = timetableFacade.rescheduleRecurring(candidate, date, option);

        then(templateService).should().findById(id);
        assertThat(underlyingTemplate.getWeekParity()).isFalse();
        assertThat(underlyingTemplate.getDay()).isEqualTo(newDay);
        assertThat(underlyingTemplate.getPeriod()).isEqualTo(newPeriod);
        assertThat(underlyingTemplate.getAuditorium()).isEqualTo(newAuditorium);

        assertThat(candidate.getDay()).isEqualTo(newDay);
        assertThat(candidate.getPeriod()).isEqualTo(newPeriod);
        assertThat(candidate.getAuditorium()).isEqualTo(newAuditorium);

        then(scheduleService).should()
                .updateAllWithSameTemplateId(candidate, date);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void reschedulePermanentlyShouldThrowNotFoundExceptionIfTemplateNotFound() {

        given(templateService.findById(anyLong())).willReturn(Optional.empty());

        assertThatExceptionOfType(NotFoundException.class).isThrownBy(
                () -> timetableFacade.rescheduleRecurring(schedule,
                        LocalDate.MAX, option))
                .withMessage("Template with ID(1) could not be found");
    }

    @Test
    public void deleteTimetableDataShouldDelegateDeleteAllToUnderlyingServices() {

        timetableFacade.deleteTimetableData();

        then(optionService).should().deleteAll();
        then(scheduleService).should().deleteAll();
        then(templateService).should().deleteAll();
    }

    @Test
    public void deleteAllDataShouldDelegateDeleteAllToUnderlyingServices() {

        timetableFacade.deleteAllData();

        then(optionService).should().deleteAll();
        then(scheduleService).should().deleteAll();
        then(templateService).should().deleteAll();
        then(auditoriumService).should().deleteAll();
        then(courseService).should().deleteAll();
        then(groupService).should().deleteAll();
        then(professorService).should().deleteAll();
        then(studentService).should().deleteAll();
    }

}
