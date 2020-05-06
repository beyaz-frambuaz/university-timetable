package com.foxminded.timetable.service.model.generator;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.foxminded.timetable.dao.jdbc.Repositories;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.ScheduleTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableModelGenerator {

    private final Repositories repositories;
    private List<ScheduleTemplate> scheduleTemplates = new ArrayList<>();

    public void generateAndSave() {

        log.info("Generating timetable model");
        populateScheduleTemplates();
        repositories.getTemplateRepository().saveAll(scheduleTemplates);
        log.info("Timetable model generated");
    }

    private List<ReschedulingOption> buildReschedulingOptions() {

        List<Auditorium> auditoriums = repositories.getAuditoriumRepository()
                .findAll();

        List<ReschedulingOption> options = Arrays.stream(DayOfWeek.values())
                .filter(day -> !day.equals(DayOfWeek.SATURDAY)
                        && !day.equals(DayOfWeek.SUNDAY))
                .flatMap(day -> Arrays.stream(Period.values())
                        .flatMap(period -> auditoriums.stream()
                                .map(auditorium -> new ReschedulingOption(0,
                                        day, period, auditorium))))
                .collect(toList());
        log.debug("Rescheduling options generated");

        return repositories.getReschedulingOptionRepository().saveAll(options);
    }

    private void populateScheduleTemplates() {

        List<Group> groups = repositories.getGroupRepository().findAll();
        List<Course> courses = repositories.getCourseRepository().findAll();
        List<Professor> professors = repositories.getProfessorRepository()
                .findAll();
        List<ReschedulingOption> options = buildReschedulingOptions();

        for (Group group : groups) {
            for (Course course : courses) {

                List<Professor> courseProfessors = getCourseProfessorsSortedByLeastWorkload(
                        professors, course);
                Optional<ScheduleTemplate> template = scheduleGroupForCourse(
                        options, courseProfessors, group, course);

                if (template.isPresent()) {
                    scheduleTemplates.add(template.get());
                } else {
                    log.warn("Could not schedule group {} for course {}", group,
                            course);
                    System.out.println(String.format(
                            "Could not schedule group %s for course %s",
                            group.getName(), course));
                }
            }
        }
        log.debug("Schedule templates generated");
    }

    private List<Professor> getCourseProfessorsSortedByLeastWorkload(
            List<Professor> professors, Course course) {

        List<Professor> courseProfessors = professors.stream()
                .filter(professor -> professor.getCourses().contains(course))
                .collect(toList());

        if (!scheduleTemplates.isEmpty()) {
            courseProfessors = courseProfessors.stream()
                    .sorted(Comparator.comparing(professor -> scheduleTemplates
                            .stream().map(ScheduleTemplate::getProfessor)
                            .filter(professorScheduled -> professorScheduled
                                    .equals(professor))
                            .collect(counting())))
                    .collect(toList());
        }
        return courseProfessors;
    }

    private Optional<ScheduleTemplate> scheduleGroupForCourse(
            List<ReschedulingOption> options, List<Professor> professors,
            Group group, Course course) {

        Optional<ScheduleTemplate> template = Optional.empty();
        Optional<ReschedulingOption> scheduleSlot = Optional.empty();

        for (Professor professor : professors) {
            boolean[][] weekParityOptions = new boolean[][] {
                    new boolean[] { false, true },
                    new boolean[] { true, false } };
            Random random = new Random();
            int randomParity = random.nextInt(2);
            for (boolean weekParity : weekParityOptions[randomParity]) {
                Collections.shuffle(options);
                scheduleSlot = options.stream()
                        .filter(option -> isScheduled(option, weekParity))
                        .filter(option -> isScheduledForProfessorAndGroup(
                                option, weekParity, group, professor))
                        .findAny();

                if (scheduleSlot.isPresent()) {
                    return Optional.of(new ScheduleTemplate(weekParity,
                            scheduleSlot.get().getDay(),
                            scheduleSlot.get().getPeriod(),
                            scheduleSlot.get().getAuditorium(), course, group,
                            professor));
                }
            }
        }
        return template;
    }

    private boolean isScheduled(ReschedulingOption option, boolean weekParity) {

        return scheduleTemplates.isEmpty() || scheduleTemplates.stream()
                .filter(template -> template.getWeekParity() == weekParity)
                .noneMatch(template -> template.getDay() == option.getDay()
                        && template.getPeriod() == option.getPeriod()
                        && template.getAuditorium()
                                .equals(option.getAuditorium()));
    }

    private boolean isScheduledForProfessorAndGroup(ReschedulingOption option,
            boolean weekParity, Group group, Professor professor) {

        return scheduleTemplates.isEmpty() || scheduleTemplates.stream()
                .filter(template -> template.getWeekParity() == weekParity)
                .filter(template -> template.getGroup().equals(group)
                        || template.getProfessor().equals(professor))
                .noneMatch(template -> template.getDay() == option.getDay()
                        && template.getPeriod() == option.getPeriod());
    }
}
