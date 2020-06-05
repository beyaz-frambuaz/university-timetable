package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.GroupDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Professor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupDao repository;

    public long count() {

        log.debug("Fetching group count from repository");
        return repository.count();
    }

    public Group save(Group group) {

        if (group.getId() == null) {
            log.debug("Adding new group {}", group);
            return repository.save(group);
        }
        log.debug("Updating group {}", group);
        return repository.update(group);

    }

    public List<Group> saveAll(List<Group> groups) {

        if (groups.isEmpty()) {
            log.debug("Recieved empty list, not saving");
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

}
