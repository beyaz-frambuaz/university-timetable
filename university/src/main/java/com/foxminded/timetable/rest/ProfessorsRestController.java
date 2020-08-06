package com.foxminded.timetable.rest;

import com.foxminded.timetable.constraints.IdValid;
import com.foxminded.timetable.exceptions.*;
import com.foxminded.timetable.model.*;
import com.foxminded.timetable.rest.assemblers.*;
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
@RequestMapping("/api/v1/timetable/professors")
@Validated
@RequiredArgsConstructor
public class ProfessorsRestController {

    private final TimetableFacade timetableFacade;
    private final ProfessorModelAssembler assembler;
    private final CourseModelAssembler courseAssembler;
    private final StudentModelAssembler studentAssembler;

    @GetMapping
    public CollectionModel<EntityModel<Professor>> findAll() {

        List<EntityModel<Professor>> models = timetableFacade.getProfessors()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                ProfessorsRestController.class).findAll()).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Professor>> addNew(
            @RequestBody @Valid Professor professor) {

        EntityModel<Professor> entityModel =
                assembler.toModel(timetableFacade.saveProfessor(professor));
        return ResponseEntity.created(
                entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    public EntityModel<Professor> findById(
            @PathVariable @IdValid("Professor") long id) {

        Professor professor = timetableFacade.getProfessor(id)
                .orElseThrow(() -> new NotFoundException(
                        "Professor with ID " + id + " could not be found"));
        return assembler.toModel(professor);
    }

    @PutMapping("/{id}")
    public EntityModel<Professor> editById(
            @PathVariable @IdValid("Professor") long id,
            @RequestBody @Valid Professor newProfessor) {

        Professor savedProfessor =
                timetableFacade.getProfessor(id).map(professor -> {
                    professor.setFirstName(newProfessor.getFirstName());
                    professor.setLastName(newProfessor.getLastName());
                    if (newProfessor.getCourses() != null) {
                        professor.setCourses(newProfessor.getCourses());
                    }
                    return timetableFacade.saveProfessor(professor);
                }).orElseGet(() -> {
                    newProfessor.setId(id);
                    return timetableFacade.saveProfessor(newProfessor);
                });
        return assembler.toModel(savedProfessor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @IdValid("Professor") long id) {

        throw new MethodNotImplementedException("professors");
    }

    @GetMapping("/{id}/courses")
    public CollectionModel<EntityModel<Course>> findProfessorCourses(
            @PathVariable @IdValid("Professor") long id) {

        Professor professor = timetableFacade.getProfessor(id)
                .orElseThrow(() -> new NotFoundException(
                        "Professor with ID " + id + " could not be found"));

        List<EntityModel<Course>> models = professor.getCourses()
                .stream()
                .map(courseAssembler::toModel)
                .collect(Collectors.toList());

        return new CollectionModel<>(models,
                linkTo(methodOn(ProfessorsRestController.class).findById(
                        id)).withSelfRel());
    }

    @GetMapping("/{id}/courses/{courseId}/students")
    public CollectionModel<EntityModel<Student>> findCourseAttendees(
            @PathVariable @IdValid("Professor") long id,
            @PathVariable @IdValid("Course") long courseId) {

        Professor professor = timetableFacade.getProfessor(id)
                .orElseThrow(() -> new NotFoundException(
                        "Professor with ID " + id + " could not be found"));
        Course course = professor.getCourses()
                .stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "Course with ID " + courseId + " could not be found"));
        List<EntityModel<Student>> models =
                timetableFacade.getCourseAttendees(course, professor)
                        .stream()
                        .map(studentAssembler::toModel)
                        .collect(Collectors.toList());

        return new CollectionModel<>(models, linkTo(methodOn(
                ProfessorsRestController.class).findCourseAttendees(id,
                courseId)).withSelfRel(),
                linkTo(methodOn(ProfessorsRestController.class).findById(
                        id)).withRel("professor"),
                linkTo(methodOn(CoursesRestController.class).findById(courseId))
                        .withRel("course"));
    }

}
