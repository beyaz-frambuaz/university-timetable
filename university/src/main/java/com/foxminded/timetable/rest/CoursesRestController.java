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
@RequestMapping("/api/v1/timetable/courses")
@Validated
@RequiredArgsConstructor
public class CoursesRestController {

    private final TimetableFacade timetableFacade;
    private final CourseModelAssembler assembler;
    private final ProfessorModelAssembler professorAssembler;

    @GetMapping
    public CollectionModel<EntityModel<Course>> findAll() {

        List<EntityModel<Course>> models = timetableFacade.getCourses()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(
                CoursesRestController.class).findAll()).withSelfRel());
    }

    @PostMapping
    public ResponseEntity<EntityModel<Course>> addNew(
            @RequestBody @Valid Course course) {

        EntityModel<Course> entityModel =
                assembler.toModel(timetableFacade.saveCourse(course));
        return ResponseEntity.created(
                entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    public EntityModel<Course> findById(
            @PathVariable @IdValid("Course") long id) {

        Course course = timetableFacade.getCourse(id)
                .orElseThrow(() -> new NotFoundException(
                        "Course with ID " + id + " could not be found"));
        return assembler.toModel(course);
    }

    @PutMapping("/{id}")
    public EntityModel<Course> editById(
            @PathVariable @IdValid("Course") long id,
            @RequestBody @Valid Course newCourse) {

        Course savedCourse = timetableFacade.getCourse(id).map(course -> {
            course.setName(newCourse.getName());
            return timetableFacade.saveCourse(course);
        }).orElseGet(() -> {
            newCourse.setId(id);
            return timetableFacade.saveCourse(newCourse);
        });
        return assembler.toModel(savedCourse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @PathVariable @IdValid("Course") long id) {

        throw new MethodNotImplementedException("courses");
    }

    @GetMapping("/{id}/professors")
    public CollectionModel<EntityModel<Professor>> findCourseProfessors(
            @PathVariable @IdValid("Course") long id) {

        Course course = timetableFacade.getCourse(id)
                .orElseThrow(() -> new NotFoundException(
                        "Course with ID " + id + " could not be found"));

        List<EntityModel<Professor>> models =
                timetableFacade.getProfessorsTeaching(course)
                        .stream()
                        .map(professorAssembler::toModel)
                        .collect(Collectors.toList());

        return new CollectionModel<>(models,
                linkTo(methodOn(CoursesRestController.class).findById(
                        id)).withSelfRel());
    }

}
