package com.shablii.timetable.api.rest;

import com.shablii.timetable.api.SchedulesApi;
import com.shablii.timetable.api.rest.assemblers.*;
import com.shablii.timetable.exceptions.*;
import com.shablii.timetable.model.*;
import com.shablii.timetable.service.TimetableFacade;
import com.shablii.timetable.service.utility.SemesterCalendar;
import com.shablii.timetable.service.utility.predicates.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class SchedulesController implements SchedulesApi {

    private final TimetableFacade timetableFacade;
    private final SemesterCalendar semesterCalendar;
    private final ScheduleModelAssembler scheduleAssembler;
    private final ProfessorModelAssembler professorAssembler;
    private final AuditoriumModelAssembler auditoriumAssembler;

    @Override
    public CollectionModel<EntityModel<Schedule>> findByDate(LocalDate date, Long professorId, Long groupId,
            Long courseId, Long auditoriumId) {

        List<EntityModel<Schedule>> models = getEntityModels(getPredicate(professorId, groupId, courseId, auditoriumId),
                date, date);
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesApi.class).findByDate(date, professorId, groupId, courseId,
                        auditoriumId)).withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<Schedule>> findByWeek(int week, Long professorId, Long groupId, Long courseId,
            Long auditoriumId) {

        LocalDate monday = semesterCalendar.getWeekMonday(week);
        LocalDate friday = semesterCalendar.getWeekFriday(week);

        List<EntityModel<Schedule>> models = getEntityModels(getPredicate(professorId, groupId, courseId, auditoriumId),
                monday, friday);
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesApi.class).findByWeek(week, professorId, groupId, courseId,
                        auditoriumId)).withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<Schedule>> findByMonth(int month, Long professorId, Long groupId, Long courseId,
            Long auditoriumId) {

        LocalDate firstOfMonth = semesterCalendar.getFirstSemesterDayOfMonth(month);
        LocalDate lastOfMonth = semesterCalendar.getLastSemesterDayOfMonth(month);

        List<EntityModel<Schedule>> models = getEntityModels(getPredicate(professorId, groupId, courseId, auditoriumId),
                firstOfMonth, lastOfMonth);
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesApi.class).findByMonth(month, professorId, groupId, courseId,
                        auditoriumId)).withSelfRel());
    }

    private List<EntityModel<Schedule>> getEntityModels(SchedulePredicate predicate, LocalDate start, LocalDate end) {

        return timetableFacade.getScheduleFor(predicate, start, end)
                .stream()
                .sorted()
                .map(scheduleAssembler::toModel)
                .collect(Collectors.toList());
    }

    private SchedulePredicate getPredicate(Long professorId, Long groupId, Long courseId, Long auditoriumId) {

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

    @Override
    public EntityModel<Schedule> findById(long id) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException("Schedule with ID " + id + " could not be found"));
        return scheduleAssembler.toModel(schedule);
    }

    @Override
    public CollectionModel<EntityModel<Schedule>> reschedule(long id, Schedule rescheduled, boolean recurring) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException("Schedule with ID " + id + " could not be found"));

        validateRescheduled(rescheduled, schedule);

        List<EntityModel<Schedule>> models;
        if (recurring) {
            ReschedulingOption option = new ReschedulingOption(rescheduled.getDay(), rescheduled.getPeriod(),
                    rescheduled.getAuditorium());
            models = timetableFacade.rescheduleRecurring(schedule, rescheduled.getDate(), option)
                    .stream()
                    .sorted()
                    .map(scheduleAssembler::toModel)
                    .collect(Collectors.toList());
        } else {
            models = Collections.singletonList(scheduleAssembler.toModel(timetableFacade.saveSchedule(rescheduled)));
        }

        return new CollectionModel<>(models, linkTo(methodOn(SchedulesApi.class).reschedule(id, rescheduled, recurring))
                .withSelfRel()
                .expand(id, rescheduled, recurring),
                linkTo(methodOn(SchedulesApi.class).findById(id)).withRel("schedule"));
    }

    private void validateRescheduled(Schedule rescheduled, Schedule schedule) {

        Map<String, String> errors = new HashMap<>();
        if (!rescheduled.getId().equals(schedule.getId())) {
            errors.put("IDs don't match", "Submitted schedule ID does not " + "match path variable ID");
        }
        if (!rescheduled.getGroup().equals(schedule.getGroup())) {
            errors.put("Groups don't match", "Submitted schedule illegally modified group");
        }
        if (!rescheduled.getCourse().equals(schedule.getCourse())) {
            errors.put("Courses don't match", "Submitted schedule illegally modified course");
        }
        if (!timetableFacade.isValidToReschedule(rescheduled)) {
            errors.put("Not valid to reschedule",
                    "Submitted schedule " + "is inconsistent with existing timetable, please make "
                            + "sure to edit schedule only per available "
                            + "professor/auditorium or data from rescheduling options");
        }
        if (!errors.isEmpty()) {
            throw new ApiException("Schedule item you sent failed validation", errors);
        }
    }

    @Override
    public CollectionModel<EntityModel<Auditorium>> findAvailableAuditoriums(long id) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException("Schedule with ID " + id + " could not be found"));

        List<EntityModel<Auditorium>> models = timetableFacade.getAvailableAuditoriums(schedule.getDate(),
                schedule.getPeriod()).stream().map(auditoriumAssembler::toModel).collect(Collectors.toList());
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesApi.class).findAvailableAuditoriums(id)).withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<Professor>> findAvailableProfessors(long id) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException("Schedule with ID " + id + " could not be found"));

        List<EntityModel<Professor>> models = timetableFacade.getAvailableProfessors(schedule.getDate(),
                schedule.getPeriod()).stream().map(professorAssembler::toModel).collect(Collectors.toList());
        return new CollectionModel<>(models,
                linkTo(methodOn(SchedulesApi.class).findAvailableProfessors(id)).withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<ReschedulingOption>> findOptions(long id, LocalDate date, Integer week) {

        Schedule schedule = timetableFacade.getSchedule(id)
                .orElseThrow(() -> new NotFoundException("Schedule with ID " + id + " could not be found"));

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
                linkTo(methodOn(SchedulesApi.class).findOptions(id, date, week)).withSelfRel());
    }

}
