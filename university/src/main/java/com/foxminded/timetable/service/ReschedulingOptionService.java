package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ReschedulingOptionRepository;
import com.foxminded.timetable.model.ReschedulingOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReschedulingOptionService {

    private final ReschedulingOptionRepository repository;

    public long count() {

        log.debug("Fetching option count from repository");
        return repository.count();
    }

    public List<ReschedulingOption> saveAll(List<ReschedulingOption> options) {

        if (options.isEmpty()) {
            log.debug("Received empty list, not saving");
            return options;
        }

        log.debug("Saving options to repository");
        return repository.saveAll(options);
    }

    public List<ReschedulingOption> findAll() {

        log.debug("Fetching options from repository");
        return repository.findAll();
    }

    public List<ReschedulingOption> findAllForDay(DayOfWeek day) {

        log.debug("Fetching options for {}", day);
        return repository.findAllByDay(day);
    }

    public Optional<ReschedulingOption> findById(long id) {

        log.debug("Fetching option ID({}) from repository", id);
        return repository.findById(id);
    }

    public void deleteAll() {

        log.debug("Removing all options");
        repository.deleteAllInBatch();
    }

}
