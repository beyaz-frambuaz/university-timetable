package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.CourseDao;
import com.foxminded.timetable.model.Course;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseDao repository;

    public long count() {

        log.debug("Fetching course count from repository");
        return repository.count();
    }

    public Course save(Course course) {

        if (course.getId() == null) {
            log.debug("Adding new course {}", course);
            return repository.save(course);
        }
        log.debug("Updating course {}", course);
        return repository.update(course);

    }

    public List<Course> saveAll(List<Course> courses) {

        if (courses.isEmpty()) {
            log.debug("Recieved empty list, not saving");
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

}
