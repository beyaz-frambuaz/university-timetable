package com.shablii.timetable.service;

import com.shablii.timetable.dao.ProfessorRepository;
import com.shablii.timetable.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository repository;

    public long count() {

        log.debug("Fetching professor count from repository");
        return repository.count();
    }

    public Professor save(Professor professor) {

        log.debug("Saving professor {}", professor);
        return repository.save(professor);
    }

    public List<Professor> saveAll(List<Professor> professors) {

        if (professors.isEmpty()) {
            log.debug("Received empty list, not saving");
            return professors;
        }

        log.debug("Saving professors to repository");
        return repository.saveAll(professors);
    }

    public List<Professor> findAll() {

        log.debug("Fetching professors from repository");
        return repository.findAll();
    }

    public Optional<Professor> findById(long id) {

        log.debug("Fetching professor ID{} from repository", id);
        return repository.findById(id);
    }

    public List<Professor> findAllByCourse(Course course) {

        log.debug("Fetching professors teaching {}", course);
        return repository.findAllByCourses(course);
    }

    public List<Professor> findAvailableFor(LocalDate date, Period period) {

        log.debug("Fetching available professors from repository");
        return repository.findAllAvailable(date, period);
    }

    public void delete(Professor professor) {

        log.debug("Removing {}", professor);
        repository.delete(professor);
    }

    public void deleteAll() {

        log.debug("Removing all professors");
        repository.deleteAllInBatch();
    }

}
