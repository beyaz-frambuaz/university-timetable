package com.foxminded.timetable.rest;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.rest.assemblers.StudentModelAssembler;
import com.foxminded.timetable.service.TimetableFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/timetable/students")
@Validated
@RequiredArgsConstructor
public class StudentsRestController {

    private final TimetableFacade timetableFacade;
    private final StudentModelAssembler assembler;

    @GetMapping
    public CollectionModel<EntityModel<Student>> findAll() {

        List<EntityModel<Student>> models = timetableFacade.getStudents()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                StudentsRestController.class).findAll()).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Student>> addNew(
            @RequestBody @Valid Student student) {

        EntityModel<Student> entityModel =
                assembler.toModel(timetableFacade.saveStudent(student));
        return ResponseEntity.created(
                entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    public EntityModel<Student> findById(
            @PathVariable @IdValid("Student") long id) {

        Student student = timetableFacade.getStudent(id)
                .orElseThrow(() -> new NotFoundException(
                        "Student with ID " + id + " could not be found"));
        return assembler.toModel(student);
    }

    @PutMapping("/{id}")
    public EntityModel<Student> editById(
            @PathVariable @IdValid("Student") long id,
            @RequestBody @Valid Student newStudent) {

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @IdValid("Student") long id) {

        throw new MethodNotImplementedException("students");
    }

}
