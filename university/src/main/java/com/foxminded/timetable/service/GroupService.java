package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.GroupRepository;
import com.foxminded.timetable.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository repository;

    public long count() {

        log.debug("Fetching group count from repository");
        return repository.count();
    }

    public Group save(Group group) {

        log.debug("Saving group {}", group);
        return repository.save(group);
    }

    public List<Group> saveAll(List<Group> groups) {

        if (groups.isEmpty()) {
            log.debug("Received empty list, not saving");
            return groups;
        }

        log.debug("Saving groups to repository");
        return repository.saveAll(groups);
    }

    public List<Group> findAllAttendingProfessorCourse(Course course,
            Professor professor) {

        log.debug("Fetching groups from repository for professor ID {} and "
                + "course ID {}", professor.getId(), course.getId());
        return repository.findAllByProfessorAndCourse(professor.getId(),
                course.getId());
    }

    public List<Group> findAll() {

        log.debug("Fetching groups from repository");
        return repository.findAll();
    }

    public Optional<Group> findById(long id) {

        log.debug("Fetching group ID{} from repository", id);
        return repository.findById(id);
    }

    public void delete(Group group) {

        log.debug("Removing {}", group);
        repository.delete(group);
    }

    public void deleteAll() {

        log.debug("Removing all groups");
        repository.deleteAllInBatch();
    }

}
