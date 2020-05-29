package com.foxminded.timetable.service.model.generator;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.TimetableService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimetableModelGenerator {

    private final TimetableService       timetableService;
    private final Random                 random = new Random();
    private       List<ScheduleTemplate> scheduleTemplates;

    public void generateAndSave() {

        log.info("Generating timetable model");
        this.scheduleTemplates = new ArrayList<>();
        populateScheduleTemplates();
        timetableService.saveTemplates(scheduleTemplates);
        log.info("Timetable model generated");
    }

    private List<ReschedulingOption> buildReschedulingOptions() {

        List<Auditorium> auditoriums = timetableService.getAuditoriums();

        List<ReschedulingOption> options = Arrays.stream(DayOfWeek.values())
                .filter(day -> !day.equals(DayOfWeek.SATURDAY) && !day.equals(
                        DayOfWeek.SUNDAY))
                .flatMap(day -> Arrays.stream(Period.values())
                        .flatMap(period -> auditoriums.stream()
                                .map(auditorium -> new ReschedulingOption(0,
                                        day, period, auditorium))))
                .collect(toList());
        log.debug("Rescheduling options generated");

        return timetableService.saveReschedulingOptions(options);
    }

    private void populateScheduleTemplates() {

        List<Group> groups = timetableService.getGroups();
        List<Course> courses = timetableService.getCourses();
        List<Professor> professors = timetableService.getProfessors();
        List<ReschedulingOption> options = buildReschedulingOptions();

        for (Group group : groups) {
            for (Course course : courses) {

                List<Professor> courseProfessors =
                        getCourseProfessorsSortedByLeastWorkload(
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

        if (!scheduleTemplates.isEmpty() && courseProfessors.size() > 1) {
            courseProfessors = courseProfessors.stream()
                    .sorted(Comparator.comparing(
                            professor -> (Long) scheduleTemplates.stream()
                                    .filter(template -> template.getProfessor()
                                            .equals(professor)
                                            && template.getCourse()
                                            .equals(course))
                                    .count()))
                    .collect(toList());
        }
        return courseProfessors;
    }

    private Optional<ScheduleTemplate> scheduleGroupForCourse(
            List<ReschedulingOption> options, List<Professor> professors,
            Group group, Course course) {

        Optional<ScheduleTemplate> template = Optional.empty();
        Optional<ReschedulingOption> scheduleSlot;

        for (Professor professor : professors) {
            boolean[][] weekParityOptions = new boolean[][] {
                    new boolean[] { false, true },
                    new boolean[] { true, false } };
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
