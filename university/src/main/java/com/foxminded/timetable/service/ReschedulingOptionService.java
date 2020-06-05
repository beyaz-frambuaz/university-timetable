package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;
import com.foxminded.timetable.service.utility.SemesterCalendar;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReschedulingOptionService {

    private final ReschedulingOptionDao repository;
    private final SemesterCalendar      semesterCalendar;

    public long count() {

        log.debug("Fetching option count from repository");
        return repository.count();
    }

    public Optional<ReschedulingOption> findById(long id) {

        log.debug("Fetching option ID({}) from repository", id);
        return repository.findById(id);
    }

    public List<ReschedulingOption> findAll() {

        log.debug("Fetching options from repository");
        return repository.findAll();
    }

    public List<ReschedulingOption> saveAll(List<ReschedulingOption> options) {

        if (options.isEmpty()) {
            log.debug("Received empty list, not saving");
            return options;
        }

        log.debug("Saving options to repository");
        return repository.saveAll(options);
    }

    public Map<LocalDate, List<ReschedulingOption>> findAllFor(
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
                        findAllDayOptionsFor(weekParity, date, candidate));
            }
        }
        return results;
    }

    private Map<LocalDate, List<ReschedulingOption>> findAllDayOptionsFor(
            boolean weekParity, LocalDate targetDate, Schedule candidate) {

        log.debug("Putting together rescheduling options for {}", targetDate);
        Map<LocalDate, List<ReschedulingOption>> dayOptions = new HashMap<>();
        List<ReschedulingOption> options =
                repository.findDayReschedulingOptionsForSchedule(weekParity,
                        targetDate, candidate);
        dayOptions.put(targetDate, options);
        return dayOptions;
    }

}
