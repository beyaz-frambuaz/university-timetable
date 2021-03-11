package com.shablii.timetable.api.rest;

import com.shablii.timetable.api.StudentsApi;
import com.shablii.timetable.api.rest.assemblers.StudentModelAssembler;
import com.shablii.timetable.exceptions.*;
import com.shablii.timetable.model.Student;
import com.shablii.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequiredArgsConstructor
public class StudentsController implements StudentsApi {

    private final TimetableFacade timetableFacade;
    private final StudentModelAssembler assembler;

    @Override
    public CollectionModel<EntityModel<Student>> findAll() {

        List<EntityModel<Student>> models = timetableFacade.getStudents()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(StudentsApi.class).findAll()).withSelfRel());
    }

    @Override
    public ResponseEntity<EntityModel<Student>> addNew(Student student) {

        EntityModel<Student> entityModel = assembler.toModel(timetableFacade.saveStudent(student));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Override
    public EntityModel<Student> findById(long id) {

        Student student = timetableFacade.getStudent(id)
                .orElseThrow(() -> new NotFoundException("Student with ID " + id + " could not be found"));
        return assembler.toModel(student);
    }

    @Override
    public EntityModel<Student> editById(long id, Student newStudent) {

        Student savedStudent = timetableFacade.getStudent(id).map(student -> {
            student.setFirstName(newStudent.getFirstName());
            student.setLastName(newStudent.getLastName());
            student.setGroup(newStudent.getGroup());
            return timetableFacade.saveStudent(student);
        }).orElseGet(() -> {
            newStudent.setId(id);
            return timetableFacade.saveStudent(newStudent);
        });
        return assembler.toModel(savedStudent);
    }

    @Override
    public ResponseEntity<Void> deleteById(long id) {

        throw new MethodNotImplementedException("students");
    }

}
