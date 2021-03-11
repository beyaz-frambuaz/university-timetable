package com.shablii.timetable.service;

import com.shablii.timetable.dao.StudentRepository;
import com.shablii.timetable.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository repository;

    public long count() {

        log.debug("Fetching student count from repository");
        return repository.count();
    }

    public Student save(Student student) {

        if (student.getGroup() != null) {

            log.debug("Saving student {}", student);
            return repository.save(student);
        }

        log.debug("Not saved: student {} has no group", student);
        return student;

    }

    public List<Student> saveAll(List<Student> students) {

        if (students.isEmpty()) {
            log.debug("Received empty list, not saving");
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
        return repository.findAllByGroupIn(groups);
    }

    public Optional<Student> findById(long id) {

        log.debug("Fetching student ID{} from repository", id);
        return repository.findById(id);
    }

    public void delete(Student student) {

        log.debug("Removing {}", student);
        repository.delete(student);
    }

    public void deleteAll() {

        log.debug("Removing all students");
        repository.deleteAllInBatch();
    }

}
