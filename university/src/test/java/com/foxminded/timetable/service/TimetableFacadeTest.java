package com.foxminded.timetable.service;

import com.foxminded.timetable.exceptions.ServiceException;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class TimetableFacadeTest {

    @Mock
    private SemesterCalendar          semesterCalendar;
    @Mock
    private AuditoriumService         auditoriumService;
    @Mock
    private ProfessorService          professorService;
    @Mock
    private GroupService              groupService;
    @Mock
    private CourseService             courseService;
    @Mock
    private StudentService            studentService;
    @Mock
    private ScheduleTemplateService   templateService;
    @Mock
    private ScheduleService           scheduleService;
    @Mock
    private ReschedulingOptionService optionService;

    @InjectMocks
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
    public void saveAuditoriumShouldDelegateToAuditoriumService() {

        Auditorium expected = mock(Auditorium.class);
        given(auditoriumService.save(any(Auditorium.class))).willReturn(
                expected);

        Auditorium actual = timetableFacade.saveAuditorium(expected);

        then(auditoriumService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveAuditoriumsShouldDelegateToAuditoriumService() {

        Auditorium auditorium = mock(Auditorium.class);
        List<Auditorium> expected = Collections.singletonList(auditorium);
        given(auditoriumService.saveAll(anyList())).willReturn(expected);

        List<Auditorium> actual = timetableFacade.saveAuditoriums(expected);

        then(auditoriumService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAuditoriumShouldDelegateToAuditoriumService() {

        long id = 1L;
        Auditorium auditorium = mock(Auditorium.class);
        Optional<Auditorium> expected = Optional.of(auditorium);
        given(auditoriumService.findById(anyLong())).willReturn(expected);
        Optional<Auditorium> actual = timetableFacade.getAuditorium(id);

        then(auditoriumService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAuditoriumsShouldDelegateToAuditoriumService() {

        Auditorium auditorium = mock(Auditorium.class);
        List<Auditorium> expected = Collections.singletonList(auditorium);
        given(auditoriumService.findAll()).willReturn(expected);

        List<Auditorium> actual = timetableFacade.getAuditoriums();

        then(auditoriumService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAvailableAuditoriumsShouldDelegateToAuditoriumService() {

        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        Auditorium auditorium = mock(Auditorium.class);
        List<Auditorium> expected = Collections.singletonList(auditorium);
        given(auditoriumService.findAvailableFor(any(LocalDate.class),
                any(Period.class))).willReturn(expected);

        List<Auditorium> actual =
                timetableFacade.getAvailableAuditoriums(date, period);

        then(auditoriumService).should().findAvailableFor(date, period);
        assertThat(actual).isEqualTo(expected);
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
    public void saveCourseShouldDelegateToCourseService() {

        Course expected = mock(Course.class);
        given(courseService.save(any(Course.class))).willReturn(expected);

        Course actual = timetableFacade.saveCourse(expected);

        then(courseService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveCoursesShouldDelegateToCourseService() {

        Course course = mock(Course.class);
        List<Course> expected = Collections.singletonList(course);
        given(courseService.saveAll(anyList())).willReturn(expected);

        List<Course> actual = timetableFacade.saveCourses(expected);

        then(courseService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getCourseShouldDelegateToCourseService() {

        long id = 1L;
        Course course = mock(Course.class);
        Optional<Course> expected = Optional.of(course);
        given(courseService.findById(anyLong())).willReturn(expected);
        Optional<Course> actual = timetableFacade.getCourse(id);

        then(courseService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getCoursesShouldDelegateToCourseService() {

        Course course = mock(Course.class);
        List<Course> expected = Collections.singletonList(course);
        given(courseService.findAll()).willReturn(expected);

        List<Course> actual = timetableFacade.getCourses();

        then(courseService).should().findAll();
        assertThat(actual).isEqualTo(expected);
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
    public void saveGroupShouldDelegateToGroupService() {

        Group expected = mock(Group.class);
        given(groupService.save(any(Group.class))).willReturn(expected);

        Group actual = timetableFacade.saveGroup(expected);

        then(groupService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveGroupsShouldDelegateToGroupService() {

        Group group = mock(Group.class);
        List<Group> expected = Collections.singletonList(group);
        given(groupService.saveAll(anyList())).willReturn(expected);

        List<Group> actual = timetableFacade.saveGroups(expected);

        then(groupService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getGroupShouldDelegateToGroupService() {

        long id = 1L;
        Group group = mock(Group.class);
        Optional<Group> expected = Optional.of(group);
        given(groupService.findById(anyLong())).willReturn(expected);

        Optional<Group> actual = timetableFacade.getGroup(id);

        then(groupService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getGroupsShouldDelegateToGroupService() {

        Group group = mock(Group.class);
        List<Group> expected = Collections.singletonList(group);
        given(groupService.findAll()).willReturn(expected);

        List<Group> actual = timetableFacade.getGroups();

        then(groupService).should().findAll();
        assertThat(actual).isEqualTo(expected);
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
    public void saveProfessorShouldDelegateToProfessorService() {

        Professor expected = mock(Professor.class);
        given(professorService.save(any(Professor.class))).willReturn(expected);

        Professor actual = timetableFacade.saveProfessor(expected);

        then(professorService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveProfessorsShouldDelegateToProfessorService() {

        Professor professor = mock(Professor.class);
        List<Professor> expected = Collections.singletonList(professor);
        given(professorService.saveAll(anyList())).willReturn(expected);

        List<Professor> actual = timetableFacade.saveProfessors(expected);

        then(professorService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getProfessorShouldDelegateToProfessorService() {

        long id = 1L;
        Professor professor = mock(Professor.class);
        Optional<Professor> expected = Optional.of(professor);
        given(professorService.findById(anyLong())).willReturn(expected);

        Optional<Professor> actual = timetableFacade.getProfessor(id);

        then(professorService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getProfessorsShouldDelegateToProfessorService() {

        Professor professor = mock(Professor.class);
        List<Professor> expected = Collections.singletonList(professor);
        given(professorService.findAll()).willReturn(expected);

        List<Professor> actual = timetableFacade.getProfessors();

        then(professorService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAvailableProfessorsShouldDelegateToProfessorService() {

        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        Professor professor = mock(Professor.class);
        List<Professor> expected = Collections.singletonList(professor);
        given(professorService.findAvailableFor(any(LocalDate.class),
                any(Period.class))).willReturn(expected);

        List<Professor> actual =
                timetableFacade.getAvailableProfessors(date, period);

        then(professorService).should().findAvailableFor(date, period);
        assertThat(actual).isEqualTo(expected);
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
    public void saveOptionsShouldDelegateToOptionService() {

        ReschedulingOption option = mock(ReschedulingOption.class);
        List<ReschedulingOption> expected = Collections.singletonList(option);
        given(optionService.saveAll(anyList())).willReturn(expected);

        List<ReschedulingOption> actual = timetableFacade.saveOptions(expected);

        then(optionService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionShouldDelegateToOptionService() {

        long id = 1L;
        ReschedulingOption option = mock(ReschedulingOption.class);
        Optional<ReschedulingOption> expected = Optional.of(option);
        given(optionService.findById(anyLong())).willReturn(expected);

        Optional<ReschedulingOption> actual = timetableFacade.getOption(id);

        then(optionService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionsShouldDelegateToOptionService() {

        ReschedulingOption option = mock(ReschedulingOption.class);
        List<ReschedulingOption> expected = Collections.singletonList(option);
        given(optionService.findAll()).willReturn(expected);

        List<ReschedulingOption> actual = timetableFacade.getOptions();

        then(optionService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getOptionsForWeekShouldDelegateAppropriateCallsToUnderlyingServices() {

        Schedule schedule = mock(Schedule.class);
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

        Schedule schedule = mock(Schedule.class);
        int week = 1;
        given(semesterCalendar.isSemesterWeek(anyInt())).willReturn(false);

        List<ReschedulingOption> actual =
                timetableFacade.getOptionsForWeek(schedule, week);

        assertThat(actual).isEmpty();
    }

    @Test
    public void getOptionsForDateShouldDelegateAppropriateCallsToUnderlyingServices() {

        Schedule schedule = mock(Schedule.class);
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
        Group group = mock(Group.class);
        Professor professor = mock(Professor.class);
        Auditorium availableAuditorium = mock(Auditorium.class);
        Auditorium busyAuditoriumOne = mock(Auditorium.class);
        Auditorium busyAuditoriumTwo = mock(Auditorium.class);

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

        Schedule schedule = mock(Schedule.class);
        given(schedule.getDay()).willReturn(day);
        given(schedule.getPeriod()).willReturn(period);
        given(schedule.getAuditorium()).willReturn(busyAuditoriumOne);
        given(schedule.getGroup()).willReturn(group);
        given(schedule.getProfessor()).willReturn(professor);
        given(scheduleService.findGeneratedInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(
                Collections.singletonList(schedule));

        ScheduleTemplate template = mock(ScheduleTemplate.class);
        given(template.getDay()).willReturn(day);
        given(template.getPeriod()).willReturn(period);
        given(template.getAuditorium()).willReturn(busyAuditoriumTwo);
        given(template.getGroup()).willReturn(group);
        given(template.getProfessor()).willReturn(professor);
        given(templateService.findAllForDay(anyBoolean(), any(DayOfWeek.class)))
                .willReturn(Collections.singletonList(template));

        Schedule candidate = mock(Schedule.class);
        Professor candidateProfessor = mock(Professor.class);
        Group candidateGroup = mock(Group.class);
        given(candidate.getProfessor()).willReturn(candidateProfessor);
        given(candidate.getGroup()).willReturn(candidateGroup);

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
        Group group = mock(Group.class);
        Professor professor = mock(Professor.class);
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
                timetableFacade.getOptionsForDate(candidate, date);

        assertThat(actual).containsOnly(availableOption)
                .doesNotContain(busyOptionOne, busyOptionTwo, busyOptionThree,
                        busyOptionFour);
    }

    @Test
    public void getOptionsForDateShouldReturnEmptyListGivenNonSemesterDate() {

        Schedule schedule = mock(Schedule.class);
        LocalDate date = LocalDate.MAX;
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                false);

        List<ReschedulingOption> actual =
                timetableFacade.getOptionsForDate(schedule, date);

        assertThat(actual).isEmpty();
    }

    @Test
    public void saveScheduleShouldDelegateToScheduleService() {

        Schedule expected = mock(Schedule.class);
        given(scheduleService.save(any(Schedule.class))).willReturn(expected);

        Schedule actual = timetableFacade.saveSchedule(expected);

        then(scheduleService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveSchedulesShouldDelegateToScheduleService() {

        Schedule schedule = mock(Schedule.class);
        List<Schedule> expected = Collections.singletonList(schedule);
        given(scheduleService.saveAll(anyList())).willReturn(expected);

        List<Schedule> actual = timetableFacade.saveSchedules(expected);

        then(scheduleService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getScheduleShouldDelegateToScheduleService() {

        long id = 1L;
        Schedule schedule = mock(Schedule.class);
        Optional<Schedule> expected = Optional.of(schedule);
        given(scheduleService.findById(anyLong())).willReturn(expected);

        Optional<Schedule> actual = timetableFacade.getSchedule(id);

        then(scheduleService).should().findById(id);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getSchedulesShouldDelegateToScheduleService() {

        Schedule schedule = mock(Schedule.class);
        List<Schedule> expected = Collections.singletonList(schedule);
        given(scheduleService.findAll()).willReturn(expected);

        List<Schedule> actual = timetableFacade.getSchedules();

        then(scheduleService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getScheduleInRangeShouldDelegateToService() {

        LocalDate date = LocalDate.MAX;
        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.findAllInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(expected);

        List<Schedule> actual = timetableFacade.getScheduleInRange(date, date);

        then(scheduleService).should().findAllInRange(date, date);
        assertThat(actual).isEqualTo(expected);
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
    public void saveTemplateShouldDelegateToTemplateService() {

        ScheduleTemplate expected = mock(ScheduleTemplate.class);
        given(templateService.save(any(ScheduleTemplate.class))).willReturn(
                expected);

        ScheduleTemplate actual = timetableFacade.saveTemplate(expected);

        then(templateService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveTemplatesShouldDelegateToTemplateService() {

        ScheduleTemplate template = mock(ScheduleTemplate.class);
        List<ScheduleTemplate> expected = Collections.singletonList(template);
        given(templateService.saveAll(anyList())).willReturn(expected);

        List<ScheduleTemplate> actual = timetableFacade.saveTemplates(expected);

        then(templateService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getTemplateShouldDelegateToTemplateService() {

        long id = 1L;
        ScheduleTemplate template = mock(ScheduleTemplate.class);
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
    public void countStudentsShouldDelegateToStudentService() {

        long expected = 1L;
        given(studentService.count()).willReturn(expected);

        long actual = timetableFacade.countStudents();

        then(studentService).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveStudentShouldDelegateToStudentService() {

        Student expected = mock(Student.class);
        given(studentService.save(any(Student.class))).willReturn(expected);

        Student actual = timetableFacade.saveStudent(expected);

        then(studentService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void saveStudentsShouldDelegateToStudentService() {

        Student student = mock(Student.class);
        List<Student> expected = Collections.singletonList(student);
        given(studentService.saveAll(anyList())).willReturn(expected);

        List<Student> actual = timetableFacade.saveStudents(expected);

        then(studentService).should().saveAll(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getStudentShouldDelegateToStudentService() {

        long id = 1L;
        Student student = mock(Student.class);
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
    public void getCourseAttendeesShouldDelegateToGroupAndStudentServices() {

        Course course = mock(Course.class);
        Professor professor = mock(Professor.class);
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
    public void substituteProfessorShouldSetProfessorAndDelegateSaveToService() {

        Schedule expected = mock(Schedule.class);
        Professor professor = mock(Professor.class);
        given(scheduleService.save(any(Schedule.class))).willReturn(expected);

        Schedule actual =
                timetableFacade.substituteProfessor(expected, professor);

        then(expected).should().setProfessor(professor);
        then(scheduleService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void rescheduleOnceShouldSetNewScheduleAttributesAndDelegateSaveToService() {

        LocalDate date = LocalDate.MAX;
        DayOfWeek day = DayOfWeek.MONDAY;
        Period period = Period.FIRST;
        Auditorium auditorium = mock(Auditorium.class);
        ReschedulingOption option = mock(ReschedulingOption.class);
        Schedule expected = mock(Schedule.class);

        given(option.getDay()).willReturn(day);
        given(option.getPeriod()).willReturn(period);
        given(option.getAuditorium()).willReturn(auditorium);
        given(scheduleService.save(any(Schedule.class))).willReturn(expected);

        Schedule actual =
                timetableFacade.rescheduleOnce(expected, date, option);

        then(expected).should().setDate(date);
        then(expected).should().setDay(day);
        then(expected).should().setPeriod(period);
        then(expected).should().setAuditorium(auditorium);
        then(scheduleService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    /*
     * 1. should request template from service
     * 2. should set template attributes
     * 3. should set schedule attributes
     * 4. should delegate update all schedules with same template id to service
     */
    @Test
    public void reschedulePermanentlyTest() {

        long templateId = 1L;
        Schedule schedule = mock(Schedule.class);
        ScheduleTemplate template = mock(ScheduleTemplate.class);
        given(schedule.getTemplate()).willReturn(template);
        given(template.getId()).willReturn(templateId);

        given(templateService.findById(anyLong())).willReturn(
                Optional.of(template));

        LocalDate date = LocalDate.MAX;
        DayOfWeek day = DayOfWeek.MONDAY;
        Period period = Period.FIRST;
        Auditorium auditorium = mock(Auditorium.class);
        ReschedulingOption option = mock(ReschedulingOption.class);

        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);
        given(option.getDay()).willReturn(day);
        given(option.getPeriod()).willReturn(period);
        given(option.getAuditorium()).willReturn(auditorium);

        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.updateAllWithSameTemplateId(any(Schedule.class),
                any(LocalDate.class))).willReturn(expected);

        List<Schedule> actual = null;
        try {
            actual = timetableFacade.reschedulePermanently(schedule, date,
                    option);
        } catch (ServiceException e) {
            fail();
        }

        then(templateService).should().findById(templateId);
        then(template).should().setWeekParity(false);
        then(template).should().setDay(day);
        then(template).should().setPeriod(period);
        then(template).should().setAuditorium(auditorium);

        then(schedule).should().setDay(day);
        then(schedule).should().setPeriod(period);
        then(schedule).should().setAuditorium(auditorium);
        then(scheduleService).should()
                .updateAllWithSameTemplateId(schedule, date);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void reschedulePermanentlyShouldThrowServiceExceptionIfTemplateNotFound() {

        long templateId = 1L;
        Schedule schedule = mock(Schedule.class);
        ScheduleTemplate template = mock(ScheduleTemplate.class);
        given(schedule.getTemplate()).willReturn(template);
        given(template.getId()).willReturn(templateId);
        LocalDate date = LocalDate.MAX;
        ReschedulingOption option = mock(ReschedulingOption.class);
        Optional<ScheduleTemplate> emptyOptional = Optional.empty();
        given(templateService.findById(anyLong())).willReturn(emptyOptional);

        assertThatExceptionOfType(ServiceException.class).isThrownBy(
                () -> timetableFacade.reschedulePermanently(schedule, date,
                        option))
                .withMessage("Template with ID(1) could not be found");
    }

}
