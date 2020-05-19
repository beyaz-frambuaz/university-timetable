package com.foxminded.timetable.service;

import com.foxminded.timetable.model.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableService {

    @Getter
    private final SemesterCalendarUtils     semesterCalendar;
    private final AuditoriumService         auditoriumService;
    private final CourseService             courseService;
    private final GroupService              groupService;
    private final ProfessorService          professorService;
    private final ReschedulingOptionService optionService;
    private final ScheduleService           scheduleService;
    private final ScheduleTemplateService   templateService;
    private final StudentService            studentService;

    public List<ScheduleTemplate> getTwoWeekSchedule() {

        return templateService.findAll();
    }

    public List<Student> getStudents() {

        return studentService.findAll();
    }

    public List<Schedule> getStudentSchedule(Student student,
            LocalDate startDate, LocalDate endDate) {

        log.debug("Filtering schedules in range {}-{} by group ID {}",
                startDate, endDate, student.getGroup().getId());

        return scheduleService.findAllInRange(startDate, endDate)
                .stream()
                .filter(s -> s.getGroup()
                        .getId()
                        .equals(student.getGroup().getId()))
                .sorted()
                .collect(toList());
    }

    public List<Schedule> getProfessorSchedule(Professor professor,
            LocalDate startDate, LocalDate endDate) {

        log.debug("Filtering schedules in range {}-{} by professor ID {}",
                startDate, endDate, professor.getId());

        return scheduleService.findAllInRange(startDate, endDate)
                .stream()
                .filter(s -> s.getProfessor().getId().equals(professor.getId()))
                .sorted()
                .collect(toList());
    }

    public List<Schedule> getAuditoriumSchedule(Auditorium auditorium,
            LocalDate startDate, LocalDate endDate) {

        log.debug("Filtering schedules in range {}-{} by auditorium ID {}",
                startDate, endDate, auditorium.getId());

        return scheduleService.findAllInRange(startDate, endDate)
                .stream()
                .filter(s -> s.getAuditorium()
                        .getId()
                        .equals(auditorium.getId()))
                .sorted()
                .collect(toList());
    }

    public List<Professor> getProfessors() {

        return professorService.findAll();
    }

    public List<Auditorium> getAuditoriums() {

        return auditoriumService.findAll();
    }

    public List<Student> getCourseAttendees(Course course,
            Professor professor) {

        List<Group> professorGroups =
                groupService.findAllAttendingProfessorCourse(
                course, professor);
        return studentService.findAllInGroups(professorGroups);
    }

    public List<Auditorium> getAvailableAuditoriums(LocalDate date,
            Period period) {

        boolean weekParity = semesterCalendar.getWeekParityOf(date);
        return auditoriumService.findAvailableFor(weekParity, date, period);
    }

    public List<Professor> getAvailableProfessors(LocalDate date,
            Period period) {

        boolean weekParity = semesterCalendar.getWeekParityOf(date);
        return professorService.findAvailableFor(weekParity, date, period);
    }

    public List<Schedule> getScheduleInRange(LocalDate startDate,
            LocalDate endDate) {

        return scheduleService.findAllInRange(startDate, endDate);
    }

    public Schedule substituteProfessor(Schedule schedule,
            Professor substitute) {

        schedule.setProfessor(substitute);
        return scheduleService.save(schedule);
    }

    public Map<LocalDate, List<ReschedulingOption>> getReschedulingOptions(
            Schedule candidate, LocalDate startDate, LocalDate endDate) {

        log.debug("Assembling rescheduling options for schedule ID {} in range "
                + "{}-{}", candidate.getId(), startDate, endDate);
        Map<LocalDate, List<ReschedulingOption>> results = new HashMap<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {
            LocalDate date = startDate.plusDays(i);
            if (semesterCalendar.isSemesterDate(date)) {
                boolean weekParity = semesterCalendar.getWeekParityOf(date);
                results.putAll(
                        optionService.findAllDayOptionsFor(weekParity, date,
                                candidate));
            }
        }
        return results;
    }

    public Schedule rescheduleOnce(Schedule candidate, LocalDate targetDate,
            ReschedulingOption targetOption) {

        log.debug("Calling repository to reschedule once");
        candidate.setDate(targetDate);
        candidate.setDay(targetOption.getDay());
        candidate.setPeriod(targetOption.getPeriod());
        candidate.setAuditorium(targetOption.getAuditorium());

        return scheduleService.save(candidate);
    }

    public List<Schedule> reschedulePermanently(Schedule candidate,
            LocalDate targetDate, ReschedulingOption targetOption) {

        log.debug("Getting underlying template to reschedule permanently");
        Optional<ScheduleTemplate> template = templateService.findById(
                candidate.getTemplateId());

        if (!template.isPresent()) {
            return Collections.emptyList();
        }
        boolean weekParity = semesterCalendar.getWeekParityOf(targetDate);
        template.get().setWeekParity(weekParity);
        template.get().setDay(targetOption.getDay());
        template.get().setPeriod(targetOption.getPeriod());
        template.get().setAuditorium(targetOption.getAuditorium());

        log.debug("Saving updated template");
        templateService.save(template.get());

        log.debug("Updating related schedules");
        candidate.setDay(targetOption.getDay());
        candidate.setPeriod(targetOption.getPeriod());
        candidate.setAuditorium(targetOption.getAuditorium());
        scheduleService.updateAll(candidate, targetDate);

        return scheduleService.findAllByTemplateId(candidate.getTemplateId());
    }

}
