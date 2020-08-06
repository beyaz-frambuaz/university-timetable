package com.foxminded.timetable.service;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.NotFoundException;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.SchedulePredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Validated
@RequiredArgsConstructor
@Slf4j
public class TimetableFacade {

    private final SemesterCalendar semesterCalendar;
    private final AuditoriumService auditoriumService;
    private final CourseService courseService;
    private final GroupService groupService;
    private final ProfessorService professorService;
    private final ReschedulingOptionService optionService;
    private final ScheduleService scheduleService;
    private final ScheduleTemplateService templateService;
    private final StudentService studentService;

    public long countAuditoriums() {

        return auditoriumService.count();
    }

    public Auditorium saveAuditorium(@NotNull @Valid Auditorium auditorium) {

        return auditoriumService.save(auditorium);
    }

    public List<Auditorium> saveAuditoriums(
            List<@Valid Auditorium> auditoriums) {

        return auditoriumService.saveAll(auditoriums);
    }

    public Optional<Auditorium> getAuditorium(@IdValid("Auditorium") long id) {

        return auditoriumService.findById(id);
    }

    public List<Auditorium> getAuditoriums() {

        return auditoriumService.findAll();
    }

    public List<Auditorium> getAvailableAuditoriums(@NotNull LocalDate date,
            @NotNull Period period) {

        return auditoriumService.findAvailableFor(date, period);
    }

    public void deleteAuditorium(@NotNull @Valid Auditorium auditorium) {

        auditoriumService.delete(auditorium);
    }

    public void deleteAllAuditoriums() {

        auditoriumService.deleteAll();
    }

    public long countCourses() {

        return courseService.count();
    }

    public Course saveCourse(@NotNull @Valid Course course) {

        return courseService.save(course);
    }

    public List<Course> saveCourses(List<@Valid Course> courses) {

        return courseService.saveAll(courses);
    }

    public Optional<Course> getCourse(@IdValid("Course") long id) {

        return courseService.findById(id);
    }

    public List<Course> getCourses() {

        return courseService.findAll();
    }

    public void deleteCourse(@NotNull @Valid Course course) {

        courseService.delete(course);
    }

    public void deleteAllCourses() {

        courseService.deleteAll();
    }

    public long countGroups() {

        return groupService.count();
    }

    public Group saveGroup(@NotNull @Valid Group group) {

        return groupService.save(group);
    }

    public List<Group> saveGroups(List<@Valid Group> groups) {

        return groupService.saveAll(groups);
    }

    public Optional<Group> getGroup(@IdValid("Group") long id) {

        return groupService.findById(id);
    }

    public List<Group> getGroups() {

        return groupService.findAll();
    }

    public Map<Group, List<Student>> getGroupedStudents() {

        List<Student> students = studentService.findAll();
        List<Group> groups = groupService.findAll();

        return groups.stream()
                .collect(Collectors.toMap(Function.identity(), group -> students
                                .stream()
                                .filter(student -> student.getGroup().equals(group))
                                .sorted(Comparator.comparing(Student::getId))
                                .collect(Collectors.toList()),
                        (list1, list2) -> list1,
                        LinkedHashMap::new));
    }

    public void deleteGroup(@NotNull @Valid Group group) {

        groupService.delete(group);
    }

    public void deleteAllGroups() {

        groupService.deleteAll();
    }

    public long countProfessors() {

        return professorService.count();
    }

    public Professor saveProfessor(@NotNull @Valid Professor professor) {

        return professorService.save(professor);
    }

    public List<Professor> saveProfessors(List<@Valid Professor> professors) {

        return professorService.saveAll(professors);
    }

    public Optional<Professor> getProfessor(@IdValid("Professor") long id) {

        return professorService.findById(id);
    }

    public List<Professor> getProfessors() {

        return professorService.findAll();
    }

    public List<Professor> getProfessorsTeaching(@Valid Course course) {

        return professorService.findAllByCourse(course);
    }

    public List<Professor> getAvailableProfessors(@NotNull LocalDate date,
            @NotNull Period period) {

        return professorService.findAvailableFor(date, period);
    }

    public void deleteProfessor(@NotNull @Valid Professor professor) {

        professorService.delete(professor);
    }

    public void deleteAllProfessors() {

        professorService.deleteAll();
    }

    public long countOptions() {

        return optionService.count();
    }

    public List<ReschedulingOption> saveOptions(
            List<@Valid ReschedulingOption> reschedulingOptions) {

        return optionService.saveAll(reschedulingOptions);
    }

    public Optional<ReschedulingOption> getOption(@IdValid("Option") long id) {

        return optionService.findById(id);
    }

    public List<ReschedulingOption> getOptions() {

        return optionService.findAll();
    }

    public List<ReschedulingOption> getOptionsForWeek(
            @NotNull @Valid Schedule candidate, @Min(1) int semesterWeek) {

        if (!semesterCalendar.isSemesterWeek(semesterWeek)) {
            return Collections.emptyList();
        }

        List<ScheduleTemplate> templates =
                templateService.findAllForWeek(semesterWeek % 2 == 0);
        List<Schedule> schedules = scheduleService.findGeneratedInRange(
                semesterCalendar.getWeekMonday(semesterWeek),
                semesterCalendar.getWeekFriday(semesterWeek));
        List<ReschedulingOption> options = optionService.findAll();

        return options.parallelStream()
                .filter(byAvailableAuditoriumAndProfessorGroupPerPeriod(
                        candidate, templates, schedules))
                .collect(Collectors.toList());
    }

    public List<ReschedulingOption> getOptionsForDate(
            @NotNull @Valid Schedule candidate, @NotNull LocalDate date) {

        if (!semesterCalendar.isSemesterDate(date)) {
            return Collections.emptyList();
        }

        List<ScheduleTemplate> templates = templateService.findAllForDay(
                semesterCalendar.getWeekParityOf(date), date.getDayOfWeek());
        List<Schedule> schedules =
                scheduleService.findGeneratedInRange(date, date);
        List<ReschedulingOption> options =
                optionService.findAllForDay(date.getDayOfWeek());

        return options.parallelStream()
                .filter(byAvailableAuditoriumAndProfessorGroupPerPeriod(
                        candidate, templates, schedules))
                .collect(Collectors.toList());
    }

    private Predicate<ReschedulingOption> byAvailableAuditoriumAndProfessorGroupPerPeriod(
            Schedule candidate, List<ScheduleTemplate> templates,
            List<Schedule> schedules) {

        return option -> schedules.parallelStream()
                .filter(schedule -> schedule.getDay().equals(option.getDay())
                        && schedule.getPeriod() == option.getPeriod())
                .noneMatch(scheduleBusy(candidate, option))
                && templates.parallelStream()
                .filter(template -> template.getDay().equals(option.getDay())
                        && template.getPeriod() == option.getPeriod())
                .noneMatch(templateBusy(candidate, option));
    }

    private Predicate<Schedule> scheduleBusy(Schedule candidate,
            ReschedulingOption option) {

        return schedule ->
                schedule.getAuditorium().equals(option.getAuditorium())
                        || schedule.getGroup().equals(candidate.getGroup())
                        || schedule.getProfessor()
                        .equals(candidate.getProfessor());
    }

    private Predicate<ScheduleTemplate> templateBusy(Schedule candidate,
            ReschedulingOption option) {

        return template ->
                template.getAuditorium().equals(option.getAuditorium())
                        || template.getGroup().equals(candidate.getGroup())
                        || template.getProfessor()
                        .equals(candidate.getProfessor());
    }

    public void deleteAllOptions() {

        optionService.deleteAll();
    }

    public Schedule saveSchedule(@NotNull @Valid Schedule schedule) {

        return scheduleService.save(schedule);
    }

    public List<Schedule> saveSchedules(List<@Valid Schedule> schedules) {

        return scheduleService.saveAll(schedules);
    }

    public Optional<Schedule> getSchedule(@IdValid("Schedule") long id) {

        return scheduleService.findById(id);
    }

    public List<Schedule> getSchedules() {

        return scheduleService.findAll();
    }

    public List<Schedule> getScheduleFor(@NotNull SchedulePredicate predicate,
            @NotNull LocalDate startDate, @NotNull LocalDate endDate) {

        return scheduleService.findAllFor(predicate, startDate, endDate);
    }

    public List<Schedule> getScheduleInRange(@NotNull LocalDate startDate,
            @NotNull LocalDate endDate) {

        return scheduleService.findAllInRange(startDate, endDate);
    }

    public void deleteAllSchedules() {

        scheduleService.deleteAll();
    }

    public long countTemplates() {

        return templateService.count();
    }

    public ScheduleTemplate saveTemplate(
            @NotNull @Valid ScheduleTemplate template) {

        return templateService.save(template);
    }

    public List<ScheduleTemplate> saveTemplates(
            List<@Valid ScheduleTemplate> templates) {

        return templateService.saveAll(templates);
    }

    public Optional<ScheduleTemplate> getTemplate(
            @IdValid("Template") long id) {

        return templateService.findById(id);
    }

    public List<ScheduleTemplate> getTwoWeekSchedule() {

        return templateService.findAll();
    }

    public void deleteAllTemplates() {

        templateService.deleteAll();
    }

    public long countStudents() {

        return studentService.count();
    }

    public Student saveStudent(@NotNull @Valid Student student) {

        return studentService.save(student);
    }

    public List<Student> saveStudents(List<@Valid Student> students) {

        return studentService.saveAll(students);
    }

    public Optional<Student> getStudent(@IdValid("Student") long id) {

        return studentService.findById(id);
    }

    public List<Student> getStudents() {

        return studentService.findAll();
    }

    public List<Student> getCourseAttendees(@NotNull @Valid Course course,
            @NotNull @Valid Professor professor) {

        List<Group> professorGroups =
                groupService.findAllAttendingProfessorCourse(course, professor);
        return studentService.findAllInGroups(professorGroups);
    }

    public void deleteStudent(@NotNull @Valid Student student) {

        studentService.delete(student);
    }

    public void deleteAllStudents() {

        studentService.deleteAll();
    }

    public Schedule substituteProfessor(@NotNull @Valid Schedule schedule,
            @NotNull @Valid Professor substitute) {

        schedule.setProfessor(substitute);
        return scheduleService.save(schedule);
    }

    public Schedule rescheduleSingle(@NotNull @Valid Schedule candidate,
            @NotNull LocalDate targetDate,
            @NotNull @Valid ReschedulingOption targetOption) {

        log.debug("Calling repository to reschedule once");
        candidate.setDate(targetDate);
        candidate.setDay(targetOption.getDay());
        candidate.setPeriod(targetOption.getPeriod());
        candidate.setAuditorium(targetOption.getAuditorium());

        return scheduleService.save(candidate);
    }

    public boolean isValidToReschedule(@NotNull @Valid Schedule candidate) {

        return scheduleService.findAllInRange(candidate.getDate(),
                candidate.getDate())
                .stream()
                .filter(schedule -> schedule.getPeriod()
                        == candidate.getPeriod())
                .noneMatch(schedule -> schedule.getAuditorium()
                        .equals(candidate.getAuditorium())
                        || schedule.getGroup().equals(candidate.getGroup())
                        || schedule.getProfessor()
                        .equals(candidate.getProfessor()));
    }

    public List<Schedule> rescheduleRecurring(
            @NotNull @Valid Schedule candidate, @NotNull LocalDate targetDate,
            @NotNull @Valid ReschedulingOption targetOption) {

        log.debug("Getting underlying template to reschedule permanently");
        ScheduleTemplate template =
                templateService.findById(candidate.getTemplate().getId())
                        .orElseThrow(() -> new NotFoundException(
                                "Template with ID(" + candidate.getTemplate()
                                        .getId() + ") could not be found"));

        log.debug("Updating template");
        boolean weekParity = semesterCalendar.getWeekParityOf(targetDate);
        template.setWeekParity(weekParity);
        template.setDay(targetOption.getDay());
        template.setPeriod(targetOption.getPeriod());
        template.setAuditorium(targetOption.getAuditorium());

        log.debug("Updating related schedules");
        candidate.setDay(targetOption.getDay());
        candidate.setPeriod(targetOption.getPeriod());
        candidate.setAuditorium(targetOption.getAuditorium());

        return scheduleService.updateAllWithSameTemplateId(candidate,
                targetDate);
    }

    public void deleteTimetableData() {

        optionService.deleteAll();
        scheduleService.deleteAll();
        templateService.deleteAll();
    }

    public void deleteAllData() {

        optionService.deleteAll();
        scheduleService.deleteAll();
        templateService.deleteAll();
        auditoriumService.deleteAll();
        courseService.deleteAll();
        groupService.deleteAll();
        professorService.deleteAll();
        studentService.deleteAll();
    }

}
