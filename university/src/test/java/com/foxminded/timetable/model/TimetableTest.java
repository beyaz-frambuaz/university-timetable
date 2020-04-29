package com.foxminded.timetable.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.dao.jdbc.Repositories;

@ExtendWith(MockitoExtension.class)
public class TimetableTest {

    long stubId = 0L;
    boolean weekParity = false;
    LocalDate from = LocalDate.of(2020, 1, 1);
    LocalDate until = LocalDate.of(2020, 1, 3);

    @Mock
    private SemesterCalendarUtils semesterCalendar;
    @Mock
    private AuditoriumDao auditoriumRepository;
    @Mock
    private CourseDao courseRepository;
    @Mock
    private ProfessorDao professorRepository;
    @Mock
    private GroupDao groupRepository;
    @Mock
    private StudentDao studentRepository;
    @Mock
    private ScheduleTemplateDao templateRepository;
    @Mock
    private ScheduleDao scheduleRepository;
    @Mock
    private ReschedulingOptionDao reschedulingOptionRepository;

    @InjectMocks
    private Repositories repositories;

    private Timetable timetable;

    @BeforeEach
    private void setUp() {
        this.timetable = new Timetable(semesterCalendar, repositories);
    }

    @Test
    public void getTwoWeekScheduleShouldRequestAllTemplatesFromRepository() {

        List<ScheduleTemplate> expected = Collections.emptyList();
        given(templateRepository.findAll()).willReturn(expected);

        List<ScheduleTemplate> actual = timetable.getTwoWeekSchedule();

        then(templateRepository).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void countStudentsShouldRequestStudentCountFromRepository() {

        int expected = 0;
        given(studentRepository.count()).willReturn(0L);

        int actual = timetable.countStudents();

        then(studentRepository).should().count();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findScheduleByStudentIdShouldReturnEmptyListGivenStudentIdNotInRepository() {

        Optional<Student> emptyStudent = Optional.empty();
        given(studentRepository.findById(stubId)).willReturn(emptyStudent);

        List<Schedule> actual = timetable.findScheduleByStudentId(stubId, from,
                until);

        then(studentRepository).should().findById(stubId);
        assertThat(actual).isEmpty();
    }

    @Test
    public void findScheduleByStudentIdShouldRequestScheduleFromRepositoryThenFilterByStudentsGroup() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(true);

        Student student = mock(Student.class);
        given(studentRepository.findById(stubId))
                .willReturn(Optional.of(student));
        Group studentGroup = mock(Group.class);
        given(student.getGroup()).willReturn(studentGroup);
        given(studentGroup.getId()).willReturn(stubId);

        Schedule studentSchedule = mock(Schedule.class);
        given(studentSchedule.getGroup()).willReturn(studentGroup);
        Schedule notStudentSchedule = mock(Schedule.class);
        Group notStudentGroup = mock(Group.class);
        given(notStudentSchedule.getGroup()).willReturn(notStudentGroup);
        given(notStudentGroup.getId()).willReturn(stubId + 1);
        List<Schedule> schedulesFromRepository = Arrays.asList(studentSchedule,
                notStudentSchedule);
        given(scheduleRepository.findAllByDate(any(LocalDate.class)))
                .willReturn(schedulesFromRepository);

        List<Schedule> actual = timetable.findScheduleByStudentId(stubId, from,
                until);

        then(scheduleRepository).should(atLeastOnce())
                .findAllByDate(any(LocalDate.class));
        assertThat(actual).contains(studentSchedule)
                .doesNotContain(notStudentSchedule);
    }

    @Test
    public void findScheduleByProfessorIdShouldRequestScheduleFromRepositoryThenFilterByProfessor() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(true);

        Schedule professorSchedule = mock(Schedule.class);
        Professor professor = mock(Professor.class);
        given(professorSchedule.getProfessor()).willReturn(professor);
        given(professor.getId()).willReturn(stubId);

        Schedule notProfessorSchedule = mock(Schedule.class);
        Professor wrongProfessor = mock(Professor.class);
        given(notProfessorSchedule.getProfessor()).willReturn(wrongProfessor);
        given(wrongProfessor.getId()).willReturn(stubId + 1);

        List<Schedule> schedulesFromRepository = Arrays
                .asList(professorSchedule, notProfessorSchedule);
        given(scheduleRepository.findAllByDate(any(LocalDate.class)))
                .willReturn(schedulesFromRepository);

        List<Schedule> actual = timetable.findScheduleByProfessorId(stubId,
                from, until);

        then(scheduleRepository).should(atLeastOnce())
                .findAllByDate(any(LocalDate.class));
        assertThat(actual).contains(professorSchedule)
                .doesNotContain(notProfessorSchedule);
    }

    @Test
    public void findScheduleByAuditoriumIdShouldRequestScheduleFromRepositoryThenFilterByAuditorium() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(true);

        Schedule auditoriumSchedule = mock(Schedule.class);
        Auditorium auditorium = mock(Auditorium.class);
        given(auditoriumSchedule.getAuditorium()).willReturn(auditorium);
        given(auditorium.getId()).willReturn(stubId);

        Schedule notAuditoriumSchedule = mock(Schedule.class);
        Auditorium wrongAuditorium = mock(Auditorium.class);
        given(notAuditoriumSchedule.getAuditorium())
                .willReturn(wrongAuditorium);
        given(wrongAuditorium.getId()).willReturn(stubId + 1);

        List<Schedule> schedulesFromRepository = Arrays
                .asList(auditoriumSchedule, notAuditoriumSchedule);
        given(scheduleRepository.findAllByDate(any(LocalDate.class)))
                .willReturn(schedulesFromRepository);

        List<Schedule> actual = timetable.findScheduleByAuditoriumId(stubId,
                from, until);

        then(scheduleRepository).should(atLeastOnce())
                .findAllByDate(any(LocalDate.class));
        assertThat(actual).contains(auditoriumSchedule)
                .doesNotContain(notAuditoriumSchedule);
    }

    @Test
    public void findProfessorsShouldRequestFromRepository() {

        List<Professor> expected = Collections.emptyList();
        given(professorRepository.findAll()).willReturn(expected);

        List<Professor> actual = timetable.findProfessors();

        then(professorRepository).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAuditoriumsShouldRequestFromRepository() {

        List<Auditorium> expected = Collections.emptyList();
        given(auditoriumRepository.findAll()).willReturn(expected);

        List<Auditorium> actual = timetable.findAuditoriums();

        then(auditoriumRepository).should().findAll();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findCourseAttendeesShouldRequestAllGroupsByProfessorCourseThenRequestAllStudentsByGroupsFromRepository() {

        List<Group> professorGroups = Collections.emptyList();
        given(groupRepository.findAllByProfessorAndCourse(anyLong(), anyLong()))
                .willReturn(professorGroups);
        List<Student> expected = Collections.emptyList();
        given(studentRepository.findAllByGroups(professorGroups))
                .willReturn(expected);

        List<Student> actual = timetable.findCourseAttendees(stubId, stubId);

        then(groupRepository).should().findAllByProfessorAndCourse(stubId,
                stubId);
        then(studentRepository).should().findAllByGroups(professorGroups);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAvailableProfessorsShouldRequestFromRepository() {

        List<Professor> expected = Collections.emptyList();
        given(semesterCalendar.getWeekParityOf(any(LocalDate.class)))
                .willReturn(weekParity);
        given(professorRepository.findAllAvailable(anyBoolean(),
                any(LocalDate.class), any(Period.class))).willReturn(expected);

        List<Professor> actual = timetable.findAvailableProfessors(from,
                Period.FIRST);

        then(professorRepository).should().findAllAvailable(weekParity, from,
                Period.FIRST);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void findAvailableAuditoriumsShouldRequestFromRepository() {

        List<Auditorium> expected = Collections.emptyList();
        given(semesterCalendar.getWeekParityOf(any(LocalDate.class)))
                .willReturn(weekParity);
        given(auditoriumRepository.findAllAvailable(anyBoolean(),
                any(LocalDate.class), any(Period.class))).willReturn(expected);

        List<Auditorium> actual = timetable.findAvailableAuditoriums(from,
                Period.FIRST);

        then(auditoriumRepository).should().findAllAvailable(weekParity, from,
                Period.FIRST);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getRangeScheduleShouldRequestScheduleFromRepositoryForEachDateInRange() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(true);

        Schedule scheduleOne = mock(Schedule.class);
        List<Schedule> dayOneSchedules = Arrays.asList(scheduleOne);
        given(scheduleRepository.findAllByDate(from))
                .willReturn(dayOneSchedules);

        Schedule scheduleTwo = mock(Schedule.class);
        List<Schedule> dayTwoSchedules = Arrays.asList(scheduleTwo);
        given(scheduleRepository.findAllByDate(from.plusDays(1)))
                .willReturn(dayTwoSchedules);

        Schedule scheduleThree = mock(Schedule.class);
        List<Schedule> dayThreeSchedules = Arrays.asList(scheduleThree);
        given(scheduleRepository.findAllByDate(from.plusDays(2)))
                .willReturn(dayThreeSchedules);

        List<Schedule> actual = timetable.getRangeSchedule(from, until);

        then(scheduleRepository).should().findAllByDate(from);
        then(scheduleRepository).should().findAllByDate(from.plusDays(1));
        then(scheduleRepository).should().findAllByDate(until);
        assertThat(actual).containsExactly(scheduleOne, scheduleTwo,
                scheduleThree);

    }

    @Test
    public void getRangeScheduleShouldGenerateAndSaveSchedulesWhenNoneFoundInRepository() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(true);
        given(semesterCalendar.getWeekParityOf(any(LocalDate.class)))
                .willReturn(weekParity);
        given(scheduleRepository.findAllByDate(any(LocalDate.class)))
                .willReturn(Collections.emptyList());
        given(templateRepository.findAllByDate(anyBoolean(),
                any(DayOfWeek.class))).willReturn(Collections.emptyList());
        given(scheduleRepository.saveAll(anyList()))
                .willReturn(Collections.emptyList());

        timetable.getRangeSchedule(from, until);

        then(scheduleRepository).should(atLeastOnce())
                .findAllByDate(any(LocalDate.class));
        then(templateRepository).should(atLeastOnce())
                .findAllByDate(anyBoolean(), any(DayOfWeek.class));
        then(scheduleRepository).should(atLeastOnce()).saveAll(anyList());
    }

    @Test
    public void getRangeScheduleShouldNotRequestSchedulesFromRepositoryForNonSemesterDates() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(false);

        List<Schedule> actual = timetable.getRangeSchedule(from, until);

        then(scheduleRepository).shouldHaveNoInteractions();
        then(templateRepository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void substituteProfessorShouldRequestRepositoryToUpdateAndReturnSchedule() {

        Schedule expected = mock(Schedule.class);
        given(scheduleRepository.findById(anyLong()))
                .willReturn(Optional.of(expected));

        Schedule actual = timetable.substituteProfessor(stubId, stubId);

        then(scheduleRepository).should().substituteProfessor(stubId, stubId);
        then(scheduleRepository).should().findById(stubId);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void getReschedulingOptionsShouldRequestOptionsFromRepositoryForEachDateInRange() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(true);
        given(semesterCalendar.getWeekParityOf(any(LocalDate.class)))
                .willReturn(weekParity);
        Schedule schedule = mock(Schedule.class);
        List<ReschedulingOption> optionsOne = Collections.emptyList();
        given(reschedulingOptionRepository
                .findDayReschedulingOptionsForSchedule(weekParity, from,
                        schedule)).willReturn(optionsOne);
        List<ReschedulingOption> optionsTwo = Collections.emptyList();
        given(reschedulingOptionRepository
                .findDayReschedulingOptionsForSchedule(weekParity,
                        from.plusDays(1), schedule)).willReturn(optionsTwo);
        List<ReschedulingOption> optionsThree = Collections.emptyList();
        given(reschedulingOptionRepository
                .findDayReschedulingOptionsForSchedule(weekParity,
                        from.plusDays(2), schedule)).willReturn(optionsThree);

        Map<LocalDate, List<ReschedulingOption>> actual = timetable
                .getReschedulingOptions(schedule, from, until);

        then(reschedulingOptionRepository).should()
                .findDayReschedulingOptionsForSchedule(weekParity, from,
                        schedule);
        then(reschedulingOptionRepository).should()
                .findDayReschedulingOptionsForSchedule(weekParity,
                        from.plusDays(1), schedule);
        then(reschedulingOptionRepository).should()
                .findDayReschedulingOptionsForSchedule(weekParity, until,
                        schedule);
        assertThat(actual).containsOnlyKeys(from, from.plusDays(1), until)
                .containsValues(optionsOne, optionsTwo, optionsThree);
    }

    @Test
    public void getReschedulingOptionsShouldNotRequestOptionsFromRepositoryForNonSemesterDates() {

        given(semesterCalendar.isSemesterDate(any(LocalDate.class)))
                .willReturn(false);
        Schedule schedule = mock(Schedule.class);

        Map<LocalDate, List<ReschedulingOption>> actual = timetable
                .getReschedulingOptions(schedule, from, until);

        then(reschedulingOptionRepository).shouldHaveNoInteractions();
        assertThat(actual).isEmpty();
    }

    @Test
    public void rescheduleOnceShouldRequestRepositoryToUpdateAndReturnSchedule() {

        Schedule expected = mock(Schedule.class);
        given(expected.getId()).willReturn(stubId);
        given(scheduleRepository.findById(anyLong()))
                .willReturn(Optional.of(expected));
        ReschedulingOption option = mock(ReschedulingOption.class);

        Schedule actual = timetable.rescheduleOnce(expected, from, option);

        then(scheduleRepository).should().reschedule(expected, from, option);
        then(scheduleRepository).should().findById(stubId);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void reschedulePermanentlyShouldRequestRepositoryToUpdateTemplateThenUpdateAndRequestAllAffectedSchedules() {

        given(semesterCalendar.getWeekParityOf(any(LocalDate.class)))
                .willReturn(weekParity);
        Schedule schedule = mock(Schedule.class);
        given(schedule.getTemplateId()).willReturn(stubId);
        given(schedule.getDate()).willReturn(from);
        List<Schedule> expected = Collections.emptyList();
        given(scheduleRepository.findAllByTemplateId(anyLong()))
                .willReturn(expected);
        ReschedulingOption option = mock(ReschedulingOption.class);
        int daysBetween = (int) ChronoUnit.DAYS.between(from, until);

        List<Schedule> actual = timetable.reschedulePermanently(schedule, until,
                option);

        then(templateRepository).should().reschedule(weekParity, stubId,
                option);
        then(scheduleRepository).should().updateAllWithTemplateId(stubId,
                option, daysBetween);
        then(scheduleRepository).should().findAllByTemplateId(stubId);
        assertThat(actual).isEqualTo(expected);
    }

}
