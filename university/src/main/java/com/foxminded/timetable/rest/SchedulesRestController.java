package com.foxminded.timetable.rest;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.rest.assemblers.*;
import com.foxminded.timetable.service.TimetableFacade;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import com.foxminded.timetable.service.utility.predicates.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/timetable/schedules")
@Validated
@RequiredArgsConstructor
public class SchedulesRestController {

    private final TimetableFacade timetableFacade;
    private final SemesterCalendar semesterCalendar;
    private final ScheduleModelAssembler scheduleAssembler;
    private final ProfessorModelAssembler professorAssembler;
    private final AuditoriumModelAssembler auditoriumAssembler;

    @GetMapping("/date")
    public CollectionModel<EntityModel<Schedule>> findByDate(
            @RequestParam("date") @NotNull(
                    message = "date must not be null") @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "professorId", required = false) @IdValid(
                    "Professor") Long professorId,
            @RequestParam(name = "groupId", required = false) @IdValid(
                    "Group") Long groupId,
            @RequestParam(name = "courseId", required = false) @IdValid(
                    "Course") Long courseId,
            @RequestParam(name = "auditoriumId", required = false) @IdValid(
                    "Auditorium") Long auditoriumId) {

        List<EntityModel<Schedule>> models = getEntityModels(
                getPredicate(professorId, groupId, courseId, auditoriumId),
                date, date);
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesRestController.class).findByDate(date,
                        professorId, groupId, courseId,
                        auditoriumId)).withSelfRel());
    }

    @GetMapping("/week")
    public CollectionModel<EntityModel<Schedule>> findByWeek(
            @RequestParam("week") @Min(1) int week,
            @RequestParam(name = "professorId", required = false) @IdValid(
                    "Professor") Long professorId,
            @RequestParam(name = "groupId", required = false) @IdValid(
                    "Group") Long groupId,
            @RequestParam(name = "courseId", required = false) @IdValid(
                    "Course") Long courseId,
            @RequestParam(name = "auditoriumId", required = false) @IdValid(
                    "Auditorium") Long auditoriumId) {

        LocalDate monday = semesterCalendar.getWeekMonday(week);
        LocalDate friday = semesterCalendar.getWeekFriday(week);

        List<EntityModel<Schedule>> models = getEntityModels(
                getPredicate(professorId, groupId, courseId, auditoriumId),
                monday, friday);
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesRestController.class).findByWeek(week,
                        professorId, groupId, courseId,
                        auditoriumId)).withSelfRel());
    }

    @GetMapping("/month")
    public CollectionModel<EntityModel<Schedule>> findByMonth(
            @RequestParam("month") @Min(1) int month,
            @RequestParam(name = "professorId", required = false) @IdValid(
                    "Professor") Long professorId,
            @RequestParam(name = "groupId", required = false) @IdValid(
                    "Group") Long groupId,
            @RequestParam(name = "courseId", required = false) @IdValid(
                    "Course") Long courseId,
            @RequestParam(name = "auditoriumId", required = false) @IdValid(
                    "Auditorium") Long auditoriumId) {

        LocalDate firstOfMonth =
                semesterCalendar.getFirstSemesterDayOfMonth(month);
        LocalDate lastOfMonth =
                semesterCalendar.getLastSemesterDayOfMonth(month);

        List<EntityModel<Schedule>> models = getEntityModels(
                getPredicate(professorId, groupId, courseId, auditoriumId),
                firstOfMonth, lastOfMonth);
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesRestController.class).findByMonth(
                        month, professorId, groupId, courseId,
                        auditoriumId)).withSelfRel());
    }

    private List<EntityModel<Schedule>> getEntityModels(
            SchedulePredicate predicate, LocalDate start, LocalDate end) {

        return timetableFacade.getScheduleFor(predicate, start, end)
                .stream()
                .sorted()
                .map(scheduleAssembler::toModel)
                .collect(Collectors.toList());
    }

    private SchedulePredicate getPredicate(Long professorId, Long groupId,
            Long courseId, Long auditoriumId) {

        SchedulePredicate predicate;
        if (professorId != null) {
            predicate = new SchedulePredicateProfessorId(professorId);
        } else if (groupId != null) {
            predicate = new SchedulePredicateGroupId(groupId);
        } else if (courseId != null) {
            predicate = new SchedulePredicateCourseId(courseId);
        } else if (auditoriumId != null) {
            predicate = new SchedulePredicateAuditoriumId(auditoriumId);
        } else {
            predicate = new SchedulePredicateNoFilter();
        }
        return predicate;
    }

    @GetMapping("/{id}")
    public EntityModel<Schedule> findById(
            @PathVariable @IdValid("Schedule") long id) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException(
                        "Schedule with ID " + id + " could not be found"));
        return scheduleAssembler.toModel(schedule);
    }

    @PutMapping("/{id}")
    public CollectionModel<EntityModel<Schedule>> reschedule(
            @PathVariable @IdValid("Schedule") long id,
            @RequestBody @Valid Schedule rescheduled,
            @RequestParam(name = "recurring",
                          defaultValue = "false") boolean recurring) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException(
                        "Schedule with ID " + id + " could not be found"));

        validateRescheduled(rescheduled, schedule);

        List<EntityModel<Schedule>> models;
        if (recurring) {
            ReschedulingOption option =
                    new ReschedulingOption(rescheduled.getDay(),
                            rescheduled.getPeriod(),
                            rescheduled.getAuditorium());
            models = timetableFacade.rescheduleRecurring(schedule,
                    rescheduled.getDate(), option)
                    .stream()
                    .sorted()
                    .map(scheduleAssembler::toModel)
                    .collect(Collectors.toList());
        } else {
            models = Collections.singletonList(scheduleAssembler.toModel(
                    timetableFacade.saveSchedule(rescheduled)));
        }

        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesRestController.class).reschedule(id,
                        rescheduled, recurring)).withSelfRel()
                        .expand(id, rescheduled, recurring),
                linkTo(methodOn(SchedulesRestController.class).findById(
                        id)).withRel("schedule"));
    }

    private void validateRescheduled(Schedule rescheduled, Schedule schedule) {

        Map<String, String> errors = new HashMap<>();
        if (!rescheduled.getId().equals(schedule.getId())) {
            errors.put("IDs don't match", "Submitted schedule ID does not "
                    + "match path variable ID");
        }
        if (!rescheduled.getGroup().equals(schedule.getGroup())) {
            errors.put("Groups don't match",
                    "Submitted schedule illegally modified group");
        }
        if (!rescheduled.getCourse().equals(schedule.getCourse())) {
            errors.put("Courses don't match",
                    "Submitted schedule illegally modified course");
        }
        if (!timetableFacade.isValidToReschedule(rescheduled)) {
            errors.put("Not valid to reschedule", "Submitted schedule "
                    + "is inconsistent with existing timetable, please make "
                    + "sure to edit schedule only per available "
                    + "professor/auditorium or data from rescheduling options");
        }
        if (!errors.isEmpty()) {
            throw new ApiException("Schedule item you sent failed validation",
                    errors);
        }
    }

    @GetMapping("/{id}/available/auditoriums")
    public CollectionModel<EntityModel<Auditorium>> findAvailableAuditoriums(
            @PathVariable @IdValid("Schedule") long id) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException(
                        "Schedule with ID " + id + " could not be found"));

        List<EntityModel<Auditorium>> models =
                timetableFacade.getAvailableAuditoriums(schedule.getDate(),
                        schedule.getPeriod())
                        .stream()
                        .map(auditoriumAssembler::toModel)
                        .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                SchedulesRestController.class).findAvailableAuditoriums(
                id)).withSelfRel());
    }

    @GetMapping("/{id}/available/professors")
    public CollectionModel<EntityModel<Professor>> findAvailableProfessors(
            @PathVariable @IdValid("Schedule") long id) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException(
                        "Schedule with ID " + id + " could not be found"));

        List<EntityModel<Professor>> models =
                timetableFacade.getAvailableProfessors(schedule.getDate(),
                        schedule.getPeriod())
                        .stream()
                        .map(professorAssembler::toModel)
                        .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                SchedulesRestController.class).findAvailableProfessors(
                id)).withSelfRel());
    }

    @GetMapping("/{id}/options")
    public CollectionModel<EntityModel<ReschedulingOption>> findOptions(
            @PathVariable @IdValid("Schedule") long id,
            @RequestParam(name = "date", required = false) @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(name = "week", required = false) @Min(
                    1) Integer week) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException(
                        "Schedule with ID " + id + " could not be found"));

        List<EntityModel<ReschedulingOption>> options = new ArrayList<>();
        if (date != null) {
            options = timetableFacade.getOptionsForDate(schedule, date)
                    .stream()
                    .map(EntityModel<ReschedulingOption>::new)
                    .collect(Collectors.toList());
        }
        if (week != null) {
            options = timetableFacade.getOptionsForWeek(schedule, week)
                    .stream()
                    .map(EntityModel<ReschedulingOption>::new)
                    .collect(Collectors.toList());
        }

        return new CollectionModel<>(options,
                linkTo(methodOn(SchedulesRestController.class).findOptions(id,
                        date, week)).withSelfRel());
    }

}
