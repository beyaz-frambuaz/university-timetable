package com.shablii.timetable.service;

import com.shablii.timetable.dao.AuditoriumRepository;
import com.shablii.timetable.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriumService {

    private final AuditoriumRepository repository;

    public long count() {

        log.debug("Fetching auditorium count from repository");
        return repository.count();
    }

    public Auditorium save(Auditorium auditorium) {

        log.debug("Saving auditorium {}", auditorium);
        return repository.save(auditorium);
    }

    public List<Auditorium> saveAll(List<Auditorium> auditoriums) {

        if (auditoriums.isEmpty()) {
            log.debug("Received empty list, not saving");
            return auditoriums;
        }

        log.debug("Saving auditoriums to repository");
        return repository.saveAll(auditoriums);
    }

    public List<Auditorium> findAll() {

        log.debug("Fetching auditoriums from repository");
        return repository.findAll();
    }

    public Optional<Auditorium> findById(long id) {

        log.debug("Fetching auditorium ID{} from repository", id);
        return repository.findById(id);
    }

    public List<Auditorium> findAvailableFor(LocalDate date, Period period) {

        log.debug("Fetching available auditoriums for {} on {}", period, date);
        return repository.findAllAvailable(date, period);
    }

    public void delete(Auditorium auditorium) {

        log.debug("Removing {}", auditorium);
        repository.delete(auditorium);
    }

    public void deleteAll() {

        log.debug("Removing all auditoriums");
        repository.deleteAllInBatch();
    }

}
