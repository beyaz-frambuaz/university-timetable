package com.foxminded.timetable.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foxminded.timetable.dao.ReschedulingOptionDao;
import com.foxminded.timetable.model.ReschedulingOption;
import com.foxminded.timetable.model.Schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReschedulingOptionService {

    private final ReschedulingOptionDao repository;

    public long count() {
        log.debug("Fetching option count from repository");
        return repository.count();
    }

    public List<ReschedulingOption> findAll() {
        log.debug("Fetching options from repository");
        return repository.findAll();
    }

    public Optional<ReschedulingOption> findById(long id) {
        log.debug("Fetching option ID{} from repository", id);
        return repository.findById(id);
    }

    public List<ReschedulingOption> saveAll(List<ReschedulingOption> options) {

        if (options.isEmpty()) {
            log.debug("Recieved empty list, not saving");
            return options;
        }

        log.debug("Saving options to repository");
        return repository.saveAll(options);
    }

    public Map<LocalDate, List<ReschedulingOption>> findAllDayOptionsFor(
            boolean weekParity, LocalDate targetDate, Schedule candidate) {

        log.debug("Putting together rescheduling options for {}", targetDate);
        Map<LocalDate, List<ReschedulingOption>> dayOptions = new HashMap<>();
        List<ReschedulingOption> options = repository
                .findDayReschedulingOptionsForSchedule(weekParity, targetDate,
                        candidate);
        dayOptions.put(targetDate, options);
        return dayOptions;
    }

}
