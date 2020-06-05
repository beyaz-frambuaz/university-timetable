package com.foxminded.timetable.service;

import com.foxminded.timetable.model.*;
import com.foxminded.timetable.exceptions.ServiceException;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableFacade {

    private final SemesterCalendar          semesterCalendar;
    private final AuditoriumService         auditoriumService;
    private final CourseService             courseService;
    private final GroupService              groupService;
    private final ProfessorService          professorService;
    private final ReschedulingOptionService optionService;
    private final ScheduleService           scheduleService;
    private final ScheduleTemplateService   templateService;
    private final StudentService            studentService;

    public long countAuditoriums() {

        return auditoriumService.count();
    }

    public Auditorium saveAuditorium(Auditorium auditorium) {

        return auditoriumService.save(auditorium);
    }

    public List<Auditorium> saveAuditoriums(List<Auditorium> auditoriums) {

        return auditoriumService.saveAll(auditoriums);
    }

    public Optional<Auditorium> getAuditorium(long id) {

        return auditoriumService.findById(id);
    }

    public List<Auditorium> getAuditoriums() {

        return auditoriumService.findAll();
    }

    public List<Auditorium> getAvailableAuditoriums(LocalDate date,
            Period period) {

        return auditoriumService.findAvailableFor(date, period);
    }

    public long countCourses() {

        return courseService.count();
    }

    public Course saveCourse(Course course) {

        return courseService.save(course);
    }

    public List<Course> saveCourses(List<Course> courses) {

        return courseService.saveAll(courses);
    }

    public Optional<Course> getCourse(long id) {

        return courseService.findById(id);
    }

    public List<Course> getCourses() {

        return courseService.findAll();
    }

    public long countGroups() {

        return groupService.count();
    }

    public Group saveGroup(Group group) {

        return groupService.save(group);
    }

    public List<Group> saveGroups(List<Group> groups) {

        return groupService.saveAll(groups);
    }

    public Optional<Group> getGroup(long id) {

        return groupService.findById(id);
    }

    public List<Group> getGroups() {

        return groupService.findAll();
    }

    public long countProfessors() {

        return professorService.count();
    }

    public Professor saveProfessor(Professor professor) {

        return professorService.save(professor);
    }

    public List<Professor> saveProfessors(List<Professor> professors) {

        return professorService.saveAll(professors);
    }

    public Optional<Professor> getProfessor(long id) {

        return professorService.findById(id);
    }

    public List<Professor> getProfessors() {

        return professorService.findAll();
    }

    public List<Professor> getAvailableProfessors(LocalDate date,
            Period period) {

        return professorService.findAvailableFor(date, period);
    }

    public long countOptions() {

        return optionService.count();
    }

    public List<ReschedulingOption> saveOptions(
            List<ReschedulingOption> reschedulingOptions) {

        return optionService.saveAll(reschedulingOptions);
    }

    public Optional<ReschedulingOption> getOption(long id) {

        return optionService.findById(id);
    }

    public List<ReschedulingOption> getOptions() {

        return optionService.findAll();
    }

    public Map<LocalDate, List<ReschedulingOption>> getOptionsFor(
            Schedule candidate, LocalDate startDate, LocalDate endDate) {

        return optionService.findAllFor(candidate, startDate, endDate);
    }

    public Schedule saveSchedule(Schedule schedule) {

        return scheduleService.save(schedule);
    }

    public List<Schedule> saveSchedules(List<Schedule> schedules) {

        return scheduleService.saveAll(schedules);
    }

    public Optional<Schedule> getSchedule(long id) {

        return scheduleService.findById(id);
    }

    public List<Schedule> getSchedules() {

        return scheduleService.findAll();
    }

    public List<Schedule> getScheduleFor(SchedulePredicate predicate,
            LocalDate startDate, LocalDate endDate) {

        return scheduleService.findAllFor(predicate, startDate, endDate);
    }

    public List<Schedule> getScheduleInRange(LocalDate startDate,
            LocalDate endDate) {

        return scheduleService.findAllInRange(startDate, endDate);
    }

    public long countTemplates() {

        return templateService.count();
    }

    public ScheduleTemplate saveTemplate(ScheduleTemplate template) {

        return templateService.save(template);
    }

    public List<ScheduleTemplate> saveTemplates(
            List<ScheduleTemplate> templates) {

        return templateService.saveAll(templates);
    }

    public Optional<ScheduleTemplate> getTemplate(long id) {

        return templateService.findById(id);
    }

    public List<ScheduleTemplate> getTwoWeekSchedule() {

        return templateService.findAll();
    }

    public long countStudents() {

        return studentService.count();
    }

    public Student saveStudent(Student student) {

        return studentService.save(student);
    }

    public List<Student> saveStudents(List<Student> students) {

        return studentService.saveAll(students);
    }

    public Optional<Student> getStudent(long id) {

        return studentService.findById(id);
    }

    public List<Student> getStudents() {

        return studentService.findAll();
    }

    public List<Student> getCourseAttendees(Course course,
            Professor professor) {

        List<Group> professorGroups =
                groupService.findAllAttendingProfessorCourse(course, professor);
        return studentService.findAllInGroups(professorGroups);
    }

    public Schedule substituteProfessor(Schedule schedule,
            Professor substitute) {

        schedule.setProfessor(substitute);
        return scheduleService.save(schedule);
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
            LocalDate targetDate, ReschedulingOption targetOption)
            throws ServiceException {

        log.debug("Getting underlying template to reschedule permanently");
        Optional<ScheduleTemplate> templateOptional =
                templateService.findById(candidate.getTemplateId());

        if (!templateOptional.isPresent()) {
            throw new ServiceException(
                    "Template with ID(" + candidate.getTemplateId()
                            + ") could not be found");
        }
        ScheduleTemplate template = templateOptional.get();

        boolean weekParity = semesterCalendar.getWeekParityOf(targetDate);
        template.setWeekParity(weekParity);
        template.setDay(targetOption.getDay());
        template.setPeriod(targetOption.getPeriod());
        template.setAuditorium(targetOption.getAuditorium());

        log.debug("Saving updated template");
        templateService.save(template);

        log.debug("Updating related schedules");
        candidate.setDay(targetOption.getDay());
        candidate.setPeriod(targetOption.getPeriod());
        candidate.setAuditorium(targetOption.getAuditorium());
        scheduleService.updateAll(candidate, targetDate);

        return scheduleService.findAllByTemplateId(candidate.getTemplateId());
    }

}
