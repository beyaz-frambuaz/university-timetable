package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ScheduleDao;
import com.foxminded.timetable.dao.ScheduleTemplateDao;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.model.ScheduleTemplate;
import com.foxminded.timetable.service.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final SemesterCalendar    semesterCalendar;
    private final ScheduleTemplateDao templateRepository;
    private final ScheduleDao         repository;

    public Schedule save(Schedule schedule) {

        if (schedule.getId() == null) {
            log.debug("Adding new schedule {}", schedule);
            return repository.save(schedule);
        }
        log.debug("Updating schedule {}", schedule);
        return repository.update(schedule);

    }

    public List<Schedule> saveAll(List<Schedule> schedules) {

        if (schedules.isEmpty()) {
            log.debug("Received empty list, not saving");
            return schedules;
        }

        log.debug("Saving schedules to repository");
        return repository.saveAll(schedules);
    }

    public void updateAll(Schedule candidate, LocalDate targetDate) {

        log.debug("Calling repository to update all schedules linked to "
                + "template ID({})", candidate.getTemplateId());
        int deltaDays = (int) ChronoUnit.DAYS.between(candidate.getDate(),
                targetDate);
        repository.updateAllWithTemplateId(candidate, deltaDays);
    }

    public Schedule findById(long id) throws ServiceException {

        log.debug("Fetching schedule ID({}) from repository", id);
        Optional<Schedule> optionalSchedule = repository.findById(id);
        if (!optionalSchedule.isPresent()) {
            log.error("Schedule with ID({}) could not be found", id);
            throw new ServiceException(
                    "Schedule with ID" + id + " could not be found");
        }

        return optionalSchedule.get();
    }

    public List<Schedule> findAll() {

        log.debug("Fetching schedules from repository");
        return repository.findAll();
    }

    public List<Schedule> findAllByTemplateId(long templateId) {

        log.debug("Fetching all schedules linked to template ID({}) from "
                + "repository", templateId);
        return repository.findAllByTemplateId(templateId);
    }

    public List<Schedule> findAllInRange(LocalDate startDate,
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
        List<ScheduleTemplate> dateTemplates = templateRepository.findAllByDate(
                weekParity, day);
        List<Schedule> dateSchedules = dateTemplates.stream()
                .map(template -> new Schedule(template, date))
                .collect(toList());

        repository.saveAll(dateSchedules);
        return repository.findAllByDate(date);
    }

}
