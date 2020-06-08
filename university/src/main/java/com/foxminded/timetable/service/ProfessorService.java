package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.ProfessorDao;
import com.foxminded.timetable.model.Period;
import com.foxminded.timetable.model.Professor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorDao repository;

    public long count() {

        log.debug("Fetching professor count from repository");
        return repository.count();
    }

    public Professor save(Professor professor) {

        if (professor.getId() == null) {
            log.debug("Adding new professor {}", professor);
            repository.save(professor);

            if (!professor.getCourses().isEmpty()) {
                log.debug("Adding professor's courses");
                repository.saveAllProfessorsCourses(
                        Collections.singletonList(professor));
            }

            return professor;
        }

        log.debug("Updating professor {}", professor);
        return repository.update(professor);
    }

    public List<Professor> saveAll(List<Professor> professors) {

        if (professors.isEmpty()) {
            log.debug("Recieved empty list, not saving");
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

    public List<Professor> findAvailableFor(LocalDate date, Period period) {

        log.debug("Fetching available professors from repository");
        return repository.findAllAvailable(date, period);
    }

}
