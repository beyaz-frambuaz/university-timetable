package com.foxminded.timetable.service.printer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.printer.Printer;
import com.foxminded.timetable.service.printer.assembler.Assembler;
import com.foxminded.timetable.service.printer.assembler.ColumnWriter;

@ExtendWith(MockitoExtension.class)
public class PrinterTest {

    private String expected = "test";
    private long id = 1L;
    private LocalDate date = LocalDate.MIN;
    private DayOfWeek day = DayOfWeek.MONDAY;
    private Period period = Period.FIRST;

    @Mock
    private Auditorium auditorium;
    @Mock
    private Assembler assembler;
    @Mock
    private Course course;
    @Mock
    private Group group;
    @Mock
    private Professor professor;
    @Mock
    private ReschedulingOption option;
    @Mock
    private Schedule schedule;
    @Mock
    private Student student;
    @Mock
    private ScheduleTemplate templateOdd;
    @Mock
    private ScheduleTemplate templateEven;

    @InjectMocks
    private Printer printer;

    @BeforeEach
    private void mockBehaviorSetUp() {
        given(assembler.assembleTable(anyList())).willReturn(expected);
    }

    @Test
    public void shouldParseScheduleTemplatesAndPrintTwoTablesUsingAssembler() {

        List<ScheduleTemplate> twoWeekSchedule = Arrays.asList(templateOdd,
                templateEven);
        List<String> weekParityOdd = Arrays.asList("Odd");
        List<String> weekParityEven = Arrays.asList("Even");
        List<String> days = Arrays.asList(day.toString());
        List<String> periods = Arrays.asList(period.toString());
        List<String> groups = Arrays.asList(expected);
        List<String> auditoriums = Arrays.asList(expected);
        List<String> courses = Arrays.asList(expected);
        List<String> professors = Arrays.asList(expected);
        List<ColumnWriter> expectedOddColumns = Arrays.asList(
                new ColumnWriter("Week", weekParityOdd),
                new ColumnWriter("Day", days),
                new ColumnWriter("Period", periods),
                new ColumnWriter("Auditorium", auditoriums),
                new ColumnWriter("Group", groups),
                new ColumnWriter("Course", courses),
                new ColumnWriter("Professor", professors));
        List<ColumnWriter> expectedEvenColumns = Arrays.asList(
                new ColumnWriter("Week", weekParityEven),
                new ColumnWriter("Day", days),
                new ColumnWriter("Period", periods),
                new ColumnWriter("Auditorium", auditoriums),
                new ColumnWriter("Group", groups),
                new ColumnWriter("Course", courses),
                new ColumnWriter("Professor", professors));
        
        given(templateOdd.getWeekParity()).willReturn(false);
        given(templateOdd.getDay()).willReturn(day);
        given(templateOdd.getPeriod()).willReturn(period);
        given(templateOdd.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn(expected);
        given(templateOdd.getGroup()).willReturn(group);
        given(group.getName()).willReturn(expected);
        given(templateOdd.getCourse()).willReturn(course);
        given(course.getName()).willReturn(expected);
        given(templateOdd.getProfessor()).willReturn(professor);
        given(professor.getFullName()).willReturn(expected);

        given(templateEven.getWeekParity()).willReturn(true);
        given(templateEven.getDay()).willReturn(day);
        given(templateEven.getPeriod()).willReturn(period);
        given(templateEven.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn(expected);
        given(templateEven.getGroup()).willReturn(group);
        given(group.getName()).willReturn(expected);
        given(templateEven.getCourse()).willReturn(course);
        given(course.getName()).willReturn(expected);
        given(templateEven.getProfessor()).willReturn(professor);
        given(professor.getFullName()).willReturn(expected);
        
        String actual = printer.printTwoWeekSchedule(twoWeekSchedule);
        
        assertThat(actual).isEqualTo(expected + expected);
        then(assembler).should().assembleTable(expectedOddColumns);
        then(assembler).should().assembleTable(expectedEvenColumns);
    }

    @Test
    public void shouldParseSchedulesAndPrintUsingAssembler() {

        List<Schedule> schedules = Arrays.asList(schedule);
        List<String> ids = Arrays.asList(Long.toString(id));
        List<String> dates = Arrays.asList(date.toString());
        List<String> days = Arrays.asList(day.toString());
        List<String> periods = Arrays.asList(period.toString());
        List<String> groups = Arrays.asList(expected);
        List<String> auditoriums = Arrays.asList(expected);
        List<String> courses = Arrays.asList(expected);
        List<String> professors = Arrays.asList(expected);
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("ID", ids), new ColumnWriter("Date", dates),
                new ColumnWriter("Day", days),
                new ColumnWriter("Period", periods),
                new ColumnWriter("Auditorium", auditoriums),
                new ColumnWriter("Group", groups),
                new ColumnWriter("Course", courses),
                new ColumnWriter("Professor", professors));

        given(schedule.getId()).willReturn(id);
        given(schedule.getDate()).willReturn(date);
        given(schedule.getDay()).willReturn(day);
        given(schedule.getPeriod()).willReturn(period);
        given(schedule.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn(expected);
        given(schedule.getGroup()).willReturn(group);
        given(group.getName()).willReturn(expected);
        given(schedule.getCourse()).willReturn(course);
        given(course.getName()).willReturn(expected);
        given(schedule.getProfessor()).willReturn(professor);
        given(professor.getFullName()).willReturn(expected);

        String actual = printer.printSchedules(schedules);

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

    @Test
    public void shouldParseStudentsAndPrintUsingAssembler() {

        List<Student> students = Arrays.asList(student);
        List<String> ids = Arrays.asList(Long.toString(id));
        List<String> names = Arrays.asList(expected);
        List<String> groups = Arrays.asList(expected);
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("Id", ids), new ColumnWriter("Student", names),
                new ColumnWriter("Group", groups));

        given(student.getId()).willReturn(id);
        given(student.getFullName()).willReturn(expected);
        given(student.getGroup()).willReturn(group);
        given(group.getName()).willReturn(expected);

        String actual = printer.printStudents(students);

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

    @Test
    public void shouldParseCoursesAndPrintUsingAssembler() {

        List<Course> courses = Arrays.asList(course);
        List<String> ids = Arrays.asList(Long.toString(id));
        List<String> names = Arrays.asList(expected);
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("Id", ids), new ColumnWriter("Course", names));

        given(course.getId()).willReturn(id);
        given(course.getName()).willReturn(expected);

        String actual = printer.printCourses(courses);

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

    @Test
    public void shouldParseAuditoriumsAndPrintUsingAssembler() {

        List<Auditorium> auditoriums = Arrays.asList(auditorium);
        List<String> ids = Arrays.asList(Long.toString(id));
        List<String> names = Arrays.asList(expected);
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("Id", ids),
                new ColumnWriter("Auditorium", names));

        given(auditorium.getId()).willReturn(id);
        given(auditorium.getName()).willReturn(expected);

        String actual = printer.printAuditoriums(auditoriums);

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

    @Test
    public void shouldParsePeriodsAndPrintUsingAssembler() {

        List<String> ids = Arrays.asList("1", "2", "3", "4", "5");
        List<String> names = Arrays.asList("08:15 - 09:45 FIRST",
                "10:00 - 11:30 SECOND", "12:30 - 14:00 THIRD",
                "14:15 - 15:45 FOURTH", "16:00 - 17:30 FIFTH");
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("Id", ids), new ColumnWriter("Period", names));

        String actual = printer.printPeriods();

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

    @Test
    public void shouldParseProfessorsAndPrintUsingAssembler() {

        List<Professor> professors = Arrays.asList(professor);
        List<Course> professorCourses = Arrays.asList(course);
        List<String> ids = Arrays.asList(Long.toString(id));
        List<String> names = Arrays.asList(expected);
        List<String> courses = Arrays.asList(expected);
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("Id", ids),
                new ColumnWriter("Professor", names),
                new ColumnWriter("Courses", courses));

        given(professor.getId()).willReturn(id);
        given(professor.getFullName()).willReturn(expected);
        given(professor.getCourses()).willReturn(professorCourses);
        given(course.getName()).willReturn(expected);

        String actual = printer.printProfessors(professors);

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

    @Test
    public void shouldParseReschedulingOptionsAndPrintUsingAssembler() {

        List<ReschedulingOption> reschedulingOption = Arrays.asList(option);
        Map<LocalDate, List<ReschedulingOption>> options = Collections
                .singletonMap(date, reschedulingOption);
        List<String> dates = Arrays.asList(date.toString());
        List<String> days = Arrays.asList(day.toString());
        List<String> periods = Arrays.asList(period.toString());
        List<String> auditoriums = Arrays.asList(expected);
        List<ColumnWriter> expectedColumns = Arrays.asList(
                new ColumnWriter("Date", dates), new ColumnWriter("Day", days),
                new ColumnWriter("Period", periods),
                new ColumnWriter("Auditorium", auditoriums));

        given(option.getDay()).willReturn(day);
        given(option.getPeriod()).willReturn(period);
        given(option.getAuditorium()).willReturn(auditorium);
        given(auditorium.getName()).willReturn(expected);

        String actual = printer.printReschedulingOptions(options);

        assertThat(actual).isEqualTo(expected);
        then(assembler).should().assembleTable(expectedColumns);
    }

}
