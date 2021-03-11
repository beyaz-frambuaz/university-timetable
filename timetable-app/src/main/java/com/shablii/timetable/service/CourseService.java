package com.shablii.timetable.service;

import com.shablii.timetable.dao.CourseRepository;
import com.shablii.timetable.model.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository repository;

    public long count() {

        log.debug("Fetching course count from repository");
        return repository.count();
    }

    public Course save(Course course) {

        log.debug("Saving course {}", course);
        return repository.save(course);
    }

    public List<Course> saveAll(List<Course> courses) {

        if (courses.isEmpty()) {
            log.debug("Received empty list, not saving");
            return courses;
        }

        log.debug("Saving courses to repository");
        return repository.saveAll(courses);
    }

    public List<Course> findAll() {

        log.debug("Fetching courses from repository");
        return repository.findAll();
    }

    public Optional<Course> findById(long id) {

        log.debug("Fetching course ID{} from repository", id);
        return repository.findById(id);
    }

    public void delete(Course course) {

        log.debug("Removing {}", course);
        repository.delete(course);
    }

    public void deleteAll() {

        log.debug("Removing all courses");
        repository.deleteAllInBatch();
    }

}
