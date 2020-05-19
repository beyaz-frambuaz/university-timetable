package com.foxminded.timetable.service;

import com.foxminded.timetable.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class TimetableServiceTest {

    @Mock
    private SemesterCalendarUtils     semesterCalendar;
    @Mock
    private AuditoriumService         auditoriumService;
    @Mock
    private ProfessorService          professorService;
    @Mock
    private GroupService              groupService;
    @Mock
    private StudentService            studentService;
    @Mock
    private ScheduleTemplateService   templateService;
    @Mock
    private ScheduleService           scheduleService;
    @Mock
    private ReschedulingOptionService reschedulingOptionService;

    @InjectMocks
    private TimetableService timetable;

    @Test
    public void getTwoWeekScheduleShouldDelegateToTemplateService() {

        List<ScheduleTemplate> expected = Collections.emptyList();
        given(templateService.findAll()).willReturn(expected);

        List<ScheduleTemplate> actual = timetable.getTwoWeekSchedule();

        then(templateService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getStudentsShouldDelegateToStudentService() {

        List<Student> expected = Collections.emptyList();
        given(studentService.findAll()).willReturn(expected);

        List<Student> actual = timetable.getStudents();

        then(studentService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getStudentScheduleShouldRequestRangeScheduleFromServiceAndFilterByStudent() {

        LocalDate date = LocalDate.MAX;
        Long id = 1L;
        Long anotherId = 2L;
        Group group = mock(Group.class);
        given(group.getId()).willReturn(id);
        Group anotherGroup = mock(Group.class);
        given(anotherGroup.getId()).willReturn(anotherId);
        Student student = mock(Student.class);
        given(student.getGroup()).willReturn(group);
        Schedule expected = mock(Schedule.class);
        given(expected.getGroup()).willReturn(group);
        Schedule notExpected = mock(Schedule.class);
        given(notExpected.getGroup()).willReturn(anotherGroup);
        List<Schedule> schedules = Arrays.asList(expected, notExpected);

        given(scheduleService.findAllInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(schedules);

        List<Schedule> actual = timetable.getStudentSchedule(student, date,
                date);

        then(scheduleService).should().findAllInRange(date, date);
        assertThat(actual).containsOnly(expected).doesNotContain(notExpected);
    }

    @Test
    public void getProfessorScheduleShouldRequestRangeScheduleFromServiceWithPredicate() {

        LocalDate date = LocalDate.MAX;
        Long id = 1L;
        Long anotherId = 2L;
        Professor professor = mock(Professor.class);
        given(professor.getId()).willReturn(id);
        Professor anotherProfessor = mock(Professor.class);
        given(anotherProfessor.getId()).willReturn(anotherId);
        Schedule expected = mock(Schedule.class);
        given(expected.getProfessor()).willReturn(professor);
        Schedule notExpected = mock(Schedule.class);
        given(notExpected.getProfessor()).willReturn(anotherProfessor);
        List<Schedule> schedules = Arrays.asList(expected, notExpected);

        given(scheduleService.findAllInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(schedules);

        List<Schedule> actual = timetable.getProfessorSchedule(professor, date,
                date);

        then(scheduleService).should().findAllInRange(date, date);
        assertThat(actual).containsOnly(expected).doesNotContain(notExpected);
    }

    @Test
    public void getAuditoriumScheduleShouldRequestRangeScheduleFromServiceWithPredicate() {

        LocalDate date = LocalDate.MAX;
        Long id = 1L;
        Long anotherId = 2L;
        Auditorium auditorium = mock(Auditorium.class);
        given(auditorium.getId()).willReturn(id);
        Auditorium anotherAuditorium = mock(Auditorium.class);
        given(anotherAuditorium.getId()).willReturn(anotherId);
        Schedule expected = mock(Schedule.class);
        given(expected.getAuditorium()).willReturn(auditorium);
        Schedule notExpected = mock(Schedule.class);
        given(notExpected.getAuditorium()).willReturn(anotherAuditorium);
        List<Schedule> schedules = Arrays.asList(expected, notExpected);

        given(scheduleService.findAllInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(schedules);

        List<Schedule> actual = timetable.getAuditoriumSchedule(auditorium,
                date, date);

        then(scheduleService).should().findAllInRange(date, date);
        assertThat(actual).containsOnly(expected).doesNotContain(notExpected);
    }

    @Test
    public void getProfessorsShouldDelegateToService() {

        List<Professor> expected = Collections.emptyList();
        given(professorService.findAll()).willReturn(expected);

        List<Professor> actual = timetable.getProfessors();

        then(professorService).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAuditoriumsShouldDelegateToService() {

        List<Auditorium> expected = Collections.emptyList();
        given(auditoriumService.findAll()).willReturn(expected);

        List<Auditorium> actual = timetable.getAuditoriums();

        then(auditoriumService).should().findAll();
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

        List<Student> actual = timetable.getCourseAttendees(course, professor);

        then(groupService).should()
                .findAllAttendingProfessorCourse(course, professor);
        then(studentService).should().findAllInGroups(professorGroups);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAvailableProfessorsShouldDelegateToService() {

        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);
        List<Professor> expected = Collections.emptyList();
        given(professorService.findAvailableFor(anyBoolean(),
                any(LocalDate.class), any(Period.class))).willReturn(expected);

        List<Professor> actual = timetable.getAvailableProfessors(date, period);

        then(professorService).should().findAvailableFor(false, date, period);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getAvailableAuditoriumsShouldDelegateToService() {

        LocalDate date = LocalDate.MAX;
        Period period = Period.FIRST;
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);
        List<Auditorium> expected = Collections.emptyList();
        given(auditoriumService.findAvailableFor(anyBoolean(),
                any(LocalDate.class), any(Period.class))).willReturn(expected);

        List<Auditorium> actual = timetable.getAvailableAuditoriums(date,
                period);

        then(auditoriumService).should().findAvailableFor(false, date, period);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getScheduleInRangeShouldDelegateToService() {

        LocalDate date = LocalDate.MAX;
        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.findAllInRange(any(LocalDate.class),
                any(LocalDate.class))).willReturn(expected);

        List<Schedule> actual = timetable.getScheduleInRange(date, date);

        then(scheduleService).should().findAllInRange(date, date);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void substituteProfessorShouldSetProfessorAndDelegateSaveToService() {

        Schedule expected = mock(Schedule.class);
        Professor professor = mock(Professor.class);
        given(scheduleService.save(any(Schedule.class))).willReturn(expected);

        Schedule actual = timetable.substituteProfessor(expected, professor);

        then(expected).should().setProfessor(professor);
        then(scheduleService).should().save(expected);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getReschedulingOptionsShouldRequestOptionsFromServiceForEachDateInRange() {

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate until = LocalDate.of(2020, 1, 3);
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                true);
        given(semesterCalendar.getWeekParityOf(
                any(LocalDate.class))).willReturn(false);
        Schedule schedule = mock(Schedule.class);
        Map<LocalDate, List<ReschedulingOption>> optionsOne =
                Collections.emptyMap();
        given(reschedulingOptionService.findAllDayOptionsFor(false, from,
                schedule)).willReturn(optionsOne);
        Map<LocalDate, List<ReschedulingOption>> optionsTwo =
                Collections.emptyMap();
        given(reschedulingOptionService.findAllDayOptionsFor(false,
                from.plusDays(1), schedule)).willReturn(optionsTwo);
        Map<LocalDate, List<ReschedulingOption>> optionsThree =
                Collections.emptyMap();
        given(reschedulingOptionService.findAllDayOptionsFor(false,
                from.plusDays(2), schedule)).willReturn(optionsThree);

        Map<LocalDate, List<ReschedulingOption>> actual =
                timetable.getReschedulingOptions(
                schedule, from, until);

        then(reschedulingOptionService).should()
                .findAllDayOptionsFor(false, from, schedule);
        then(reschedulingOptionService).should()
                .findAllDayOptionsFor(false, from.plusDays(1), schedule);
        then(reschedulingOptionService).should()
                .findAllDayOptionsFor(false, until, schedule);
        assertThat(actual).isEmpty();
    }

    @Test
    public void getReschedulingOptionsShouldNotRequestOptionsFromServiceForNonSemesterDates() {

        LocalDate from = LocalDate.of(2020, 1, 1);
        LocalDate until = LocalDate.of(2020, 1, 3);
        given(semesterCalendar.isSemesterDate(any(LocalDate.class))).willReturn(
                false);
        Schedule schedule = mock(Schedule.class);

        Map<LocalDate, List<ReschedulingOption>> actual =
                timetable.getReschedulingOptions(
                schedule, from, until);

        then(reschedulingOptionService).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
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

        Schedule actual = timetable.rescheduleOnce(expected, date, option);

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
     * 3. should delegate save template to service
     * 4. should set schedule attributes
     * 5. should delegate update all to service
     * 6. should request and return all affected schedules from service
     */
    @Test
    public void reschedulePermanentlyTest() {

        long templateId = 1L;
        Schedule schedule = mock(Schedule.class);
        given(schedule.getTemplateId()).willReturn(templateId);

        ScheduleTemplate template = mock(ScheduleTemplate.class);
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

        given(templateService.save(any(ScheduleTemplate.class))).willReturn(
                template);

        List<Schedule> expected = Collections.emptyList();
        given(scheduleService.findAllByTemplateId(anyLong())).willReturn(
                expected);

        List<Schedule> actual = timetable.reschedulePermanently(schedule, date,
                option);

        then(templateService).should().findById(templateId);
        then(template).should().setWeekParity(false);
        then(template).should().setDay(day);
        then(template).should().setPeriod(period);
        then(template).should().setAuditorium(auditorium);
        then(templateService).should().save(template);

        then(schedule).should().setDay(day);
        then(schedule).should().setPeriod(period);
        then(schedule).should().setAuditorium(auditorium);
        then(scheduleService).should().updateAll(schedule, date);
        then(scheduleService).should().findAllByTemplateId(templateId);
        assertThat(actual).isEqualTo(expected);
    }

}
