package com.foxminded.university.timetable.model.generator;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import com.foxminded.university.timetable.model.Course;
import com.foxminded.university.timetable.model.Group;
import com.foxminded.university.timetable.model.Period;
import com.foxminded.university.timetable.model.Professor;
import com.foxminded.university.timetable.model.ReschedulingOption;
import com.foxminded.university.timetable.model.ScheduleTemplate;
import com.foxminded.university.timetable.model.Timetable;
import com.foxminded.university.timetable.model.University;

public class TimetableModelGenerator {
    private Timetable timetable;
    private University university;

    public TimetableModelGenerator(University university) {
        this.university = university;
    }

    public Timetable generateTimetable() {
        Set<ReschedulingOption> reschedulingOptions = buildReschedulingOptions();
        this.timetable = new Timetable(university.getSemesterProperties(),
                reschedulingOptions);
        populateScheduleTemplates();

        return timetable;
    }

    private Set<ReschedulingOption> buildReschedulingOptions() {
        return Arrays.stream(DayOfWeek.values())
                .filter(day -> !day.equals(DayOfWeek.SATURDAY)
                        && !day.equals(DayOfWeek.SUNDAY))
                .flatMap(day -> Arrays.stream(Period.values())
                        .flatMap(period -> university.getAuditoriums().stream()
                                .map(auditorium -> new ReschedulingOption(day,
                                        period, auditorium))))
                .collect(toSet());
    }

    private void populateScheduleTemplates() {
        for (Group group : university.getGroups()) {
            for (Course course : group.getCourses()) {
                List<Professor> professors = getCourseProfessorsSortedByLeastWorkload(
                        course);

                Optional<ScheduleTemplate> scheduleTemplate = scheduleGroupForCourse(
                        professors, group, course);

                if (scheduleTemplate.isPresent()) {
                    timetable.addScheduleTemplate(scheduleTemplate.get());
                } else {
                    System.out.println(String.format(
                            "Could not schedule group %s for course %s",
                            group.getName(), course));
                }
            }
        }
    }

    private List<Professor> getCourseProfessorsSortedByLeastWorkload(
            Course course) {
        List<Professor> courseProfessors = university.getProfessors().stream()
                .filter(professor -> professor.getCourses().contains(course))
                .collect(toList());

        if (!timetable.getScheduleTemplates().isEmpty()) {
            courseProfessors = courseProfessors.stream()
                    .sorted(Comparator.comparing(professor -> timetable
                            .getScheduleTemplates().stream()
                            .map(ScheduleTemplate::getProfessor)
                            .filter(professorScheduled -> professorScheduled
                                    .equals(professor))
                            .collect(counting())))
                    .collect(toList());
        }
        return courseProfessors;
    }

    private Optional<ScheduleTemplate> scheduleGroupForCourse(
            List<Professor> professors, Group group, Course course) {

        Optional<ScheduleTemplate> template = Optional.empty();
        Optional<ReschedulingOption> scheduleSlot = Optional.empty();

        for (Professor professor : professors) {
            boolean[][] weekParityOptions = new boolean[][] {
                    new boolean[] { false, true },
                    new boolean[] { true, false } };
            Random random = new Random();
            int randomParity = random.nextInt(2);
            for (boolean weekParity : weekParityOptions[randomParity]) {
                scheduleSlot = timetable.getReschedulingOptions().stream()
                        .filter(option -> isScheduled(option, weekParity))
                        .filter(option -> isScheduledForProfessorAndGroup(
                                option, weekParity, group, professor))
                        .findAny();

                if (scheduleSlot.isPresent()) {
                    return Optional.of(new ScheduleTemplate(
                            university.getSemesterProperties().getStartDate(),
                            weekParity, scheduleSlot.get().getDay(),
                            scheduleSlot.get().getPeriod(),
                            scheduleSlot.get().getAuditorium(), course, group,
                            professor));
                }
            }
        }
        return template;
    }

    private boolean isScheduled(ReschedulingOption option, boolean weekParity) {
        return timetable.getScheduleTemplates().isEmpty() || timetable
                .getScheduleTemplates().stream()
                .filter(template -> template.getWeekParity() == weekParity)
                .noneMatch(template -> template.getDay() == option.getDay()
                        && template.getPeriod() == option.getPeriod()
                        && template.getAuditorium()
                                .equals(option.getAuditorium()));
    }

    private boolean isScheduledForProfessorAndGroup(ReschedulingOption option,
            boolean weekParity, Group group, Professor professor) {

        return timetable.getScheduleTemplates().isEmpty() || timetable
                .getScheduleTemplates().stream()
                .filter(template -> template.getWeekParity() == weekParity)
                .filter(template -> template.getGroup().equals(group)
                        || template.getProfessor().equals(professor))
                .noneMatch(template -> template.getDay() == option.getDay()
                        && template.getPeriod() == option.getPeriod());
    }
}
