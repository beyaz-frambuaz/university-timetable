package com.foxminded.timetable.service;

import com.foxminded.timetable.dao.StudentDao;
import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.model.Group;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.service.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentDao repository;

    public long count() {

        log.debug("Fetching student count from repository");
        return repository.count();
    }

    public Student save(Student student) {

        if (student.getGroup() != null) {

            if (student.getId() == null) {
                log.debug("Adding new student {}", student);
                return repository.save(student);
            }

            log.debug("Updating student {}", student);
            return repository.update(student);
        }

        log.debug("Not saved: student {} has no group", student);
        return student;

    }

    public List<Student> saveAll(List<Student> students) {

        if (students.isEmpty()) {
            log.debug("Recieved empty list, not saving");
            return students;
        }

        log.debug("Saving students to repository");
        return repository.saveAll(students);
    }

    public List<Student> findAll() {

        log.debug("Fetching students from repository");
        return repository.findAll();
    }

    public List<Student> findAllInGroups(List<Group> groups) {

        log.debug("Fetching students by groups from repository");
        return repository.findAllInGroups(groups);
    }

    public Student findById(long id) throws ServiceException {

        log.debug("Fetching student ID{} from repository", id);
        Optional<Student> optionalStudent = repository.findById(id);
        if (!optionalStudent.isPresent()) {
            log.error("Student with ID{} could not be found", id);
            throw new ServiceException(
                    "Student with ID" + id + " could not be found");
        }
        return optionalStudent.get();
    }

}
