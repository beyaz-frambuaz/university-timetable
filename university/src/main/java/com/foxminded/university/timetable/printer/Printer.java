package com.foxminded.university.timetable.printer;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.foxminded.university.timetable.model.Auditorium;
import com.foxminded.university.timetable.model.Course;
import com.foxminded.university.timetable.model.Period;
import com.foxminded.university.timetable.model.Professor;
import com.foxminded.university.timetable.model.ReschedulingOption;
import com.foxminded.university.timetable.model.Schedule;
import com.foxminded.university.timetable.model.ScheduleTemplate;
import com.foxminded.university.timetable.model.Student;
import com.foxminded.university.timetable.printer.assembler.Assembler;
import com.foxminded.university.timetable.printer.assembler.ColumnWriter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Printer {
    private final Assembler assembler;

    public String printTwoWeekSchedule(List<ScheduleTemplate> twoWeekSchedule) {
        StringBuilder builder = new StringBuilder();
        twoWeekSchedule.stream()
                .collect(groupingBy(ScheduleTemplate::getWeekParity)).values()
                .forEach(weekTemplates -> builder
                        .append(printScheduleTemplates(weekTemplates)));
        return builder.toString();
    }

    private String printScheduleTemplates(List<ScheduleTemplate> templates) {
        List<String> weekParity = templates.stream()
                .map(template -> template.getWeekParity() ? "Even" : "Odd")
                .collect(toList());
        List<String> days = templates.stream()
                .map(template -> template.getDay().toString())
                .collect(toList());
        List<String> periods = templates.stream()
                .map(template -> template.getPeriod().toString())
                .collect(toList());
        List<String> auditoriums = templates.stream()
                .map(template -> template.getAuditorium().getName())
                .collect(toList());
        List<String> groups = templates.stream()
                .map(template -> template.getGroup().getName())
                .collect(toList());
        List<String> courses = templates.stream()
                .map(template -> template.getCourse().getName())
                .collect(toList());
        List<String> professors = templates.stream()
                .map(template -> template.getProfessor().getFullName())
                .collect(toList());
        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Week", weekParity),
                        new ColumnWriter("Day", days),
                        new ColumnWriter("Period", periods),
                        new ColumnWriter("Auditorium", auditoriums),
                        new ColumnWriter("Group", groups),
                        new ColumnWriter("Course", courses),
                        new ColumnWriter("Professor", professors)));
    }

    public String printSchedules(List<Schedule> schedules) {
        List<String> dates = schedules.stream()
                .map(schedule -> schedule.getDate().toString())
                .collect(toList());
        List<String> days = schedules.stream()
                .map(schedule -> schedule.getDay().toString())
                .collect(toList());
        List<String> periods = schedules.stream()
                .map(schedule -> schedule.getPeriod().toString())
                .collect(toList());
        List<String> auditoriums = schedules.stream()
                .map(schedule -> schedule.getAuditorium().getName())
                .collect(toList());
        List<String> groups = schedules.stream()
                .map(schedule -> schedule.getGroup().getName())
                .collect(toList());
        List<String> courses = schedules.stream()
                .map(schedule -> schedule.getCourse().getName())
                .collect(toList());
        List<String> professors = schedules.stream()
                .map(schedule -> schedule.getProfessor().getFullName())
                .collect(toList());
        return assembler.assembleTable(Arrays.asList(
                new ColumnWriter("Date", dates), new ColumnWriter("Day", days),
                new ColumnWriter("Period", periods),
                new ColumnWriter("Auditorium", auditoriums),
                new ColumnWriter("Group", groups),
                new ColumnWriter("Course", courses),
                new ColumnWriter("Professor", professors)));
    }

    public String printStudents(List<Student> attendees) {
        List<String> ids = IntStream.rangeClosed(1, attendees.size())
                .mapToObj(Integer::toString).collect(toList());
        List<String> names = attendees.stream().map(Student::getFullName)
                .collect(toList());
        List<String> groups = attendees.stream()
                .map(student -> student.getGroup().getName()).collect(toList());
        return assembler.assembleTable(Arrays.asList(
                new ColumnWriter("Id", ids), new ColumnWriter("Student", names),
                new ColumnWriter("Group", groups)));
    }

    public String printCourses(List<Course> courses) {
        List<String> ids = IntStream.rangeClosed(1, courses.size())
                .mapToObj(Integer::toString).collect(toList());
        List<String> names = courses.stream().map(Course::getName)
                .collect(toList());
        return assembler
                .assembleTable(Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Course", names)));
    }

    public String printAuditoriums(List<Auditorium> auditoriums) {
        List<String> ids = IntStream.rangeClosed(1, auditoriums.size())
                .mapToObj(Integer::toString).collect(toList());
        List<String> names = auditoriums.stream().map(Auditorium::getName)
                .collect(toList());
        return assembler
                .assembleTable(Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Auditorium", names)));
    }

    public String printPeriods() {
        List<String> ids = IntStream.rangeClosed(1, Period.values().length)
                .mapToObj(Integer::toString).collect(toList());
        List<String> names = Arrays.stream(Period.values())
                .map(Period::toString).collect(toList());
        return assembler
                .assembleTable(Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Period", names)));
    }

    public String printProfessors(List<Professor> professors) {
        List<String> ids = IntStream.rangeClosed(1, professors.size())
                .mapToObj(Integer::toString).collect(toList());
        List<String> names = professors.stream().map(Professor::getFullName)
                .collect(toList());
        List<String> courses = professors.stream()
                .map(professor -> professor.getCourses().stream()
                        .map(Course::getName).collect(joining(", ")))
                .collect(toList());

        return assembler
                .assembleTable(Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Professor", names),
                        new ColumnWriter("Courses", courses)));
    }

    public String printReschedulingOptions(
            Map<LocalDate, List<ReschedulingOption>> options) {
        List<String> dates = options.keySet().stream().sorted()
                .flatMap(date -> {
                    int size = options.get(date).size();
                    return Collections.nCopies(size, date).stream()
                            .map(LocalDate::toString);
                }).collect(toList());
        List<String> days = options.values().stream()
                .flatMap(dayOptions -> dayOptions.stream()).sorted()
                .map(option -> option.getDay().toString()).collect(toList());
        List<String> periods = options.values().stream()
                .flatMap(dayOptions -> dayOptions.stream()).sorted()
                .map(option -> option.getPeriod().toString()).collect(toList());
        List<String> auditoriums = options.values().stream()
                .flatMap(dayOptions -> dayOptions.stream()).sorted()
                .map(option -> option.getAuditorium().getName())
                .collect(toList());
        return assembler.assembleTable(Arrays.asList(
                new ColumnWriter("Date", dates), new ColumnWriter("Day", days),
                new ColumnWriter("Period", periods),
                new ColumnWriter("Auditorium", auditoriums)));
    }
}
