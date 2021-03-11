package com.shablii.timetable.service;

import com.shablii.timetable.dao.*;
import com.shablii.timetable.model.*;
import com.shablii.timetable.service.utility.SemesterCalendar;
import com.shablii.timetable.service.utility.predicates.SchedulePredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final SemesterCalendar semesterCalendar;
    private final ScheduleTemplateRepository templateRepository;
    private final ScheduleRepository repository;

    public Schedule save(Schedule schedule) {

        log.debug("Saving schedule {}", schedule);
        return repository.save(schedule);
    }

    public List<Schedule> saveAll(List<Schedule> schedules) {

        if (schedules.isEmpty()) {
            log.debug("Received empty list, not saving");
            return schedules;
        }

        log.debug("Saving schedules to repository");
        return repository.saveAll(schedules);
    }

    public List<Schedule> updateAllWithSameTemplateId(Schedule candidate, LocalDate targetDate) {

        long templateId = candidate.getTemplate().getId();

        log.debug("Fetching all schedules linked to template ID({})", templateId);
        List<Schedule> allByTemplateId = repository.findAllByTemplateId(templateId);
        long deltaDays = ChronoUnit.DAYS.between(candidate.getDate(), targetDate);

        for (Schedule schedule : allByTemplateId) {
            schedule.setDate(schedule.getDate().plusDays(deltaDays));
            schedule.setDay(candidate.getDay());
            schedule.setPeriod(candidate.getPeriod());
            schedule.setAuditorium(candidate.getAuditorium());
        }

        return allByTemplateId;
    }

    public Optional<Schedule> findById(long id) {

        log.debug("Fetching schedule ID({}) from repository", id);
        return repository.findById(id);
    }

    public List<Schedule> findAll() {

        log.debug("Fetching schedules from repository");
        return repository.findAll();
    }

    public List<Schedule> findGeneratedInRange(LocalDate startDate, LocalDate endDate) {

        return repository.findAllByDateBetween(startDate, endDate);
    }

    public List<Schedule> findAllFor(SchedulePredicate predicate, LocalDate startDate, LocalDate endDate) {

        log.debug("Filtering schedules in range {}-{} by {}", startDate, endDate, predicate.getCriteria());

        return findAllInRange(startDate, endDate).stream().filter(predicate).sorted().collect(toList());
    }

    public List<Schedule> findAllInRange(LocalDate startDate, LocalDate endDate) {

        log.debug("Fetching schedules in range {}-{} from repository", startDate, endDate);
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

    private List<Schedule> lookupSchedule(LocalDate date) {

        log.debug("Fetching schedules for {} from repository", date);
        List<Schedule> dateSchedules = repository.findAllByDate(date);

        if (dateSchedules.isEmpty()) {
            log.debug("Nothing found, generating schedules for {}", date);
            dateSchedules = generateAndSave(date);
        }
        return dateSchedules;
    }

    private List<Schedule> generateAndSave(LocalDate date) {

        boolean weekParity = semesterCalendar.getWeekParityOf(date);
        DayOfWeek day = date.getDayOfWeek();
        List<ScheduleTemplate> dateTemplates = templateRepository.findAllByWeekParityAndDay(weekParity, day);
        List<Schedule> dateSchedules = dateTemplates.stream()
                .map(template -> new Schedule(template, date))
                .collect(toList());

        return repository.saveAll(dateSchedules);
    }

    public void deleteAll() {

        log.debug("Removing all schedules");
        repository.deleteAllInBatch();
    }

}
