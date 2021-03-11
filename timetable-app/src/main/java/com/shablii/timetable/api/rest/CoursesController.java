package com.shablii.timetable.api.rest;

import com.shablii.timetable.api.CoursesApi;
import com.shablii.timetable.api.rest.assemblers.*;
import com.shablii.timetable.exceptions.*;
import com.shablii.timetable.model.*;
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
public class CoursesController implements CoursesApi {

    private final TimetableFacade timetableFacade;
    private final CourseModelAssembler assembler;
    private final ProfessorModelAssembler professorAssembler;

    @Override
    public CollectionModel<EntityModel<Course>> findAll() {

        List<EntityModel<Course>> models = timetableFacade.getCourses()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(CoursesApi.class).findAll()).withSelfRel());
    }

    @Override
    public ResponseEntity<EntityModel<Course>> addNew(Course course) {

        EntityModel<Course> entityModel = assembler.toModel(timetableFacade.saveCourse(course));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Override
    public EntityModel<Course> findById(long id) {

        Course course = timetableFacade.getCourse(id)
                .orElseThrow(() -> new NotFoundException("Course with ID " + id + " could not be found"));
        return assembler.toModel(course);
    }

    @Override
    public EntityModel<Course> editById(long id, Course newCourse) {

        Course savedCourse = timetableFacade.getCourse(id).map(course -> {
            course.setName(newCourse.getName());
            return timetableFacade.saveCourse(course);
        }).orElseGet(() -> {
            newCourse.setId(id);
            return timetableFacade.saveCourse(newCourse);
        });
        return assembler.toModel(savedCourse);
    }

    @Override
    public ResponseEntity<Void> deleteById(long id) {

        throw new MethodNotImplementedException("courses");
    }

    @Override
    public CollectionModel<EntityModel<Professor>> findCourseProfessors(long id) {

        Course course = timetableFacade.getCourse(id)
                .orElseThrow(() -> new NotFoundException("Course with ID " + id + " could not be found"));

        List<EntityModel<Professor>> models = timetableFacade.getProfessorsTeaching(course)
                .stream()
                .map(professorAssembler::toModel)
                .collect(Collectors.toList());

        return new CollectionModel<>(models, linkTo(methodOn(CoursesApi.class).findById(id)).withSelfRel());
    }

}
