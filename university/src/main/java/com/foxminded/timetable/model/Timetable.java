package com.foxminded.timetable.model;

import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foxminded.timetable.dao.jdbc.Repositories;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class Timetable {

    @Getter
    private final SemesterCalendarUtils semesterCalendar;
    private final Repositories repositories;

    public List<ScheduleTemplate> getTwoWeekSchedule() {
        log.debug("Fetching schedule templates from repository");
        return repositories.getTemplateRepository().findAll();
    }

    public int countStudents() {
        log.debug("Fetching student count from repository");
        return (int) repositories.getStudentRepository().count();
    }

    public List<Schedule> findScheduleByStudentId(int id, LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching student from repository");
        Optional<Student> student = repositories.getStudentRepository()
                .findById(id);
        if (!student.isPresent()) {
            log.debug("Student with ID {} no found in repository", id);
            return Collections.emptyList();
        }

        long groupId = student.get().getGroup().getId();

        log.debug("Filtering schedules in range {}-{} by group ID {}",
                startDate, endDate, groupId);
        return getRangeSchedule(startDate, endDate).stream()
                .filter(schedule -> schedule.getGroup().getId() == groupId)
                .collect(toList());
    }

    public List<Schedule> findScheduleByProfessorId(long id,
            LocalDate startDate, LocalDate endDate) {

        log.debug("Filtering schedules in range {}-{} by professor ID {}",
                startDate, endDate, id);
        return getRangeSchedule(startDate, endDate).stream()
                .filter(schedule -> schedule.getProfessor().getId() == id)
                .collect(toList());
    }

    public List<Schedule> findScheduleByAuditoriumId(long id,
            LocalDate startDate, LocalDate endDate) {

        log.debug("Filtering schedules in range {}-{} by auditorium ID {}",
                startDate, endDate, id);
        return getRangeSchedule(startDate, endDate).stream()
                .filter(schedule -> schedule.getAuditorium().getId() == id)
                .collect(toList());
    }

    public List<Professor> findProfessors() {
        log.debug("Fetching professors from repository");
        return repositories.getProfessorRepository().findAll();
    }

    public List<Auditorium> findAuditoriums() {
        log.debug("Fetching auditoriums from repository");
        return repositories.getAuditoriumRepository().findAll();
    }

    public List<Student> findCourseAttendees(long courseId, long professorId) {

        log.debug(
                "Fetching groups from repository for professor ID {} and course ID {}",
                professorId, courseId);
        List<Group> professorGroups = repositories.getGroupRepository()
                .findAllByProfessorAndCourse(professorId, courseId);
        log.debug("Fetching students from repository by professors groups");
        return repositories.getStudentRepository()
                .findAllByGroups(professorGroups);
    }

    public List<Auditorium> findAvailableAuditoriums(LocalDate date,
            Period period) {

        log.debug("Fetching available auditoriums from repository");
        boolean weekParity = semesterCalendar.getWeekParityOf(date);
        return repositories.getAuditoriumRepository()
                .findAllAvailable(weekParity, date, period);
    }

    public List<Professor> findAvailableProfessors(LocalDate date,
            Period period) {

        log.debug("Fetching available professors from repository");
        boolean weekParity = semesterCalendar.getWeekParityOf(date);
        return repositories.getProfessorRepository()
                .findAllAvailable(weekParity, date, period);
    }

    public List<Schedule> getRangeSchedule(LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching schedules in range {}-{} from repository",
                startDate, endDate);
        List<Schedule> schedules = new ArrayList<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {
            LocalDate date = startDate.plusDays(i);
            if (semesterCalendar.isSemesterDate(date)) {
                schedules.addAll(lookupSchedule(date));
            }
        }
        return schedules;
    }

    public Schedule substituteProfessor(long scheduleId, long professorId) {

        log.debug("Calling repository to substitute professor");
        repositories.getScheduleRepository().substituteProfessor(scheduleId,
                professorId);
        return repositories.getScheduleRepository().findById(scheduleId).get();
    }

    public Map<LocalDate, List<ReschedulingOption>> getReschedulingOptions(
            Schedule candidate, LocalDate startDate, LocalDate endDate) {

        log.debug(
                "Assembling rescheduling options for schedule ID {} in range {}-{}",
                candidate.getId(), startDate, endDate);
        Map<LocalDate, List<ReschedulingOption>> results = new HashMap<>();
        long daysBetweenDates = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (long i = 0; i < daysBetweenDates; i++) {
            LocalDate date = startDate.plusDays(i);
            if (semesterCalendar.isSemesterDate(date)) {
                results.putAll(getDayReschedulingOptions(candidate, date));
            }
        }
        return results;
    }

    public Schedule rescheduleOnce(Schedule candidate, LocalDate targetDate,
            ReschedulingOption targetOption) {

        log.debug("Calling repository to reschedule once");
        repositories.getScheduleRepository().reschedule(candidate, targetDate,
                targetOption);
        return repositories.getScheduleRepository().findById(candidate.getId())
                .get();
    }

    public List<Schedule> reschedulePermanently(Schedule candidate,
            LocalDate targetDate, ReschedulingOption targetOption) {

        log.debug("Calling repository to reschedule permanently");
        boolean weekParity = semesterCalendar.getWeekParityOf(targetDate);
        long templateId = candidate.getTemplateId();
        repositories.getTemplateRepository().reschedule(weekParity, templateId,
                targetOption);

        updateSchedules(candidate, targetDate, targetOption);

        return repositories.getScheduleRepository()
                .findAllByTemplateId(templateId);
    }

    private void updateSchedules(Schedule candidate, LocalDate targetDate,
            ReschedulingOption targetOption) {

        log.debug(
                "Calling repository to update all schedules linked to template ID {}",
                candidate.getTemplateId());
        int deltaDays = (int) ChronoUnit.DAYS.between(candidate.getDate(),
                targetDate);
        repositories.getScheduleRepository().updateAllWithTemplateId(
                candidate.getTemplateId(), targetOption, deltaDays);

    }

    private Map<LocalDate, List<ReschedulingOption>> getDayReschedulingOptions(
            Schedule candidate, LocalDate targetDate) {

        log.debug("Fetching rescheduling options from repository for {}",
                targetDate);
        Map<LocalDate, List<ReschedulingOption>> result = new HashMap<>();
        boolean weekParity = semesterCalendar.getWeekParityOf(targetDate);
        List<ReschedulingOption> options = repositories
                .getReschedulingOptionRepository()
                .findDayReschedulingOptionsForSchedule(weekParity, targetDate,
                        candidate);
        result.put(targetDate, options);
        return result;
    }

    private List<Schedule> lookupSchedule(LocalDate date) {

        log.debug("Fetching schedules for {} from repository", date);
        List<Schedule> dateSchedules = repositories.getScheduleRepository()
                .findAllByDate(date);

        if (dateSchedules.isEmpty()) {
            log.debug("Nothing found, generating schedules for {}", date);
            dateSchedules = generateAndSaveSchedule(date);
        }
        return dateSchedules;
    }

    private List<Schedule> generateAndSaveSchedule(LocalDate date) {

        boolean weekParity = semesterCalendar.getWeekParityOf(date);
        DayOfWeek day = date.getDayOfWeek();
        List<ScheduleTemplate> dateTemplates = repositories
                .getTemplateRepository().findAllByDate(weekParity, day);
        List<Schedule> dateSchedules = dateTemplates.stream()
                .map(template -> new Schedule(template, date))
                .collect(toList());

        repositories.getScheduleRepository().saveAll(dateSchedules);
        return repositories.getScheduleRepository().findAllByDate(date);
    }

}
