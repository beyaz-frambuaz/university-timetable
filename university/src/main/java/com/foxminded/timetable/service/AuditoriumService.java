package com.foxminded.timetable.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.foxminded.timetable.dao.AuditoriumDao;
import com.foxminded.timetable.model.Auditorium;
import com.foxminded.timetable.model.Period;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriumService {

    private final AuditoriumDao repository;

    public long count() {
        log.debug("Fetching auditorium count from repository");
        return repository.count();
    }

    public Auditorium save(Auditorium auditorium) {

        if (auditorium.getId() == null) {
            log.debug("Adding new auditorium {}", auditorium);
            return repository.save(auditorium);
        }
        log.debug("Updating auditorium {}", auditorium);
        return repository.update(auditorium);

    }

    public List<Auditorium> saveAll(List<Auditorium> auditoriums) {

        if (auditoriums.isEmpty()) {
            log.debug("Recieved empty list, not saving");
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

    public List<Auditorium> findAvailableFor(boolean weekParity, LocalDate date,
            Period period) {

        log.debug("Fetching available auditoriums from repository");
        return repository.findAllAvailable(weekParity, date, period);
    }

}
