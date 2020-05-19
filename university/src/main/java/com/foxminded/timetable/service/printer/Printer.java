package com.foxminded.timetable.service.printer;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.printer.assembler.Assembler;
import com.foxminded.timetable.service.printer.assembler.ColumnWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class Printer {

    private final Assembler assembler;

    public String printTwoWeekSchedule(List<ScheduleTemplate> twoWeekSchedule) {

        log.debug("Printing two week schedule");
        StringBuilder builder = new StringBuilder();
        twoWeekSchedule.stream()
                .sorted()
                .collect(groupingBy(ScheduleTemplate::getWeekParity))
                .values()
                .forEach(weekTemplates -> builder.append(
                        printScheduleTemplates(weekTemplates)));
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

        log.debug("Printing schedules");
        schedules.sort(Comparator.naturalOrder());
        List<String> ids = schedules.stream()
                .map(schedule -> Long.toString(schedule.getId()))
                .collect(toList());
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
        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("ID", ids),
                        new ColumnWriter("Date", dates),
                        new ColumnWriter("Day", days),
                        new ColumnWriter("Period", periods),
                        new ColumnWriter("Auditorium", auditoriums),
                        new ColumnWriter("Group", groups),
                        new ColumnWriter("Course", courses),
                        new ColumnWriter("Professor", professors)));
    }

    public String printStudents(List<Student> students) {

        log.debug("Printing students");
        students.sort(Comparator.comparingLong(Student::getId));
        List<String> ids = students.stream()
                .map(student -> Long.toString(student.getId()))
                .collect(toList());
        List<String> names = students.stream()
                .map(Student::getFullName)
                .collect(toList());
        List<String> groups = students.stream()
                .map(student -> student.getGroup().getName())
                .collect(toList());

        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Student", names),
                        new ColumnWriter("Group", groups)));
    }

    public String printCourses(List<Course> courses) {

        log.debug("Printing courses");
        courses.sort(Comparator.comparingLong(Course::getId));
        List<String> ids = courses.stream()
                .map(course -> Long.toString(course.getId()))
                .collect(toList());
        List<String> names = courses.stream()
                .map(Course::getName)
                .collect(toList());

        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Course", names)));
    }

    public String printAuditoriums(List<Auditorium> auditoriums) {

        log.debug("Printing auditoriums");
        auditoriums.sort(Comparator.comparingLong(Auditorium::getId));
        List<String> ids = auditoriums.stream()
                .map(auditorium -> Long.toString(auditorium.getId()))
                .collect(toList());
        List<String> names = auditoriums.stream()
                .map(Auditorium::getName)
                .collect(toList());

        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Auditorium", names)));
    }

    public String printPeriods() {

        log.debug("Printing periods");
        List<String> ids = IntStream.rangeClosed(1, Period.values().length)
                .mapToObj(Integer::toString)
                .collect(toList());
        List<String> names = Arrays.stream(Period.values())
                .map(Period::toString)
                .collect(toList());

        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Period", names)));
    }

    public String printProfessors(List<Professor> professors) {

        log.debug("Printing professors");
        professors.sort(Comparator.comparingLong(Professor::getId));
        List<String> ids = professors.stream()
                .map(professor -> Long.toString(professor.getId()))
                .collect(toList());
        List<String> names = professors.stream()
                .map(Professor::getFullName)
                .collect(toList());
        List<String> courses = professors.stream()
                .map(professor -> professor.getCourses()
                        .stream()
                        .map(Course::getName)
                        .collect(joining(", ")))
                .collect(toList());

        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Id", ids),
                        new ColumnWriter("Professor", names),
                        new ColumnWriter("Courses", courses)));
    }

    public String printReschedulingOptions(
            Map<LocalDate, List<ReschedulingOption>> options) {

        log.debug("Printing rescheduling options");
        List<String> dates = options.keySet()
                .stream()
                .sorted()
                .flatMap(date -> {
                    int size = options.get(date).size();
                    return Collections.nCopies(size, date)
                            .stream()
                            .map(LocalDate::toString);
                })
                .collect(toList());
        List<String> days = options.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted()
                .map(option -> option.getDay().toString())
                .collect(toList());
        List<String> periods = options.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted()
                .map(option -> option.getPeriod().toString())
                .collect(toList());
        List<String> auditoriums = options.values()
                .stream()
                .flatMap(Collection::stream)
                .sorted()
                .map(option -> option.getAuditorium().getName())
                .collect(toList());

        return assembler.assembleTable(
                Arrays.asList(new ColumnWriter("Date", dates),
                        new ColumnWriter("Day", days),
                        new ColumnWriter("Period", periods),
                        new ColumnWriter("Auditorium", auditoriums)));
    }

}
