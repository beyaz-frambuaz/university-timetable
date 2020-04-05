package com.foxminded.university.timetable.model;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class University {
    private final List<Auditorium> auditoriums;
    private final List<Course> courses;
    private final List<Group> groups;
    private final List<Professor> professors;
    private final List<Student> students;
    private SemesterProperties semesterProperties;
    private Timetable timetable;

    public List<Student> getCourseAttendees(Course course,
            Professor professor) {
        List<Group> professorGroups = timetable.getScheduleTemplates().stream()
                .filter(template -> template.getCourse().equals(course)
                        && template.getProfessor().equals(professor))
                .map(ScheduleTemplate::getGroup).collect(toList());
        return students.stream()
                .filter(student -> professorGroups.contains(student.getGroup()))
                .collect(toList());
    }

    public List<Auditorium> getAvailableAuditoriums(LocalDate date,
            Period period) {
        List<Auditorium> busyAuditoriums = timetable
                .getPeriodSchedule(date, period).stream()
                .map(Schedule::getAuditorium).collect(toList());
        List<Auditorium> availableAuditoriums = new ArrayList<>(auditoriums);
        availableAuditoriums.removeAll(busyAuditoriums);
        return availableAuditoriums;
    }

    public List<Professor> getAvailableProfessors(LocalDate date,
            Period period) {
        List<Professor> busyProfessors = timetable
                .getPeriodSchedule(date, period).stream()
                .map(Schedule::getProfessor).collect(toList());
        List<Professor> availableProfessors = new ArrayList<>(professors);
        availableProfessors.removeAll(busyProfessors);
        return availableProfessors;
    }

    public List<Schedule> getStudentSchedule(Student student,
            LocalDate startDate, LocalDate endDate) {
        return timetable.getRangeSchedule(startDate, endDate).stream().filter(
                schedule -> schedule.getGroup().equals(student.getGroup()))
                .sorted().collect(toList());
    }

    public List<Schedule> getProfessorSchedule(Professor professor,
            LocalDate startDate, LocalDate endDate) {
        return timetable.getRangeSchedule(startDate, endDate).stream()
                .filter(schedule -> schedule.getProfessor().equals(professor))
                .sorted().collect(toList());
    }

    public List<Schedule> getAuditoriumSchedule(Auditorium auditorium,
            LocalDate startDate, LocalDate endDate) {
        return timetable.getRangeSchedule(startDate, endDate).stream()
                .filter(schedule -> schedule.getAuditorium().equals(auditorium))
                .sorted().collect(toList());
    }
}
