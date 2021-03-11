package com.shablii.timetable.api.rest;

import com.shablii.timetable.api.*;
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
public class ProfessorsController implements ProfessorsApi {

    private final TimetableFacade timetableFacade;
    private final ProfessorModelAssembler assembler;
    private final CourseModelAssembler courseAssembler;
    private final StudentModelAssembler studentAssembler;

    @Override
    public CollectionModel<EntityModel<Professor>> findAll() {

        List<EntityModel<Professor>> models = timetableFacade.getProfessors()
                .stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return new CollectionModel<>(models, linkTo(methodOn(ProfessorsApi.class).findAll()).withSelfRel());
    }

    @Override
    public ResponseEntity<EntityModel<Professor>> addNew(Professor professor) {

        EntityModel<Professor> entityModel = assembler.toModel(timetableFacade.saveProfessor(professor));
        return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
    }

    @Override
    public EntityModel<Professor> findById(long id) {

        Professor professor = timetableFacade.getProfessor(id)
                .orElseThrow(() -> new NotFoundException("Professor with ID " + id + " could not be found"));
        return assembler.toModel(professor);
    }

    @Override
    public EntityModel<Professor> editById(long id, Professor newProfessor) {

        Professor savedProfessor = timetableFacade.getProfessor(id).map(professor -> {
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

    @Override
    public ResponseEntity<Void> deleteById(long id) {

        throw new MethodNotImplementedException("professors");
    }

    @Override
    public CollectionModel<EntityModel<Course>> findProfessorCourses(long id) {

        Professor professor = timetableFacade.getProfessor(id)
                .orElseThrow(() -> new NotFoundException("Professor with ID " + id + " could not be found"));

        List<EntityModel<Course>> models = professor.getCourses()
                .stream()
                .map(courseAssembler::toModel)
                .collect(Collectors.toList());

        return new CollectionModel<>(models, linkTo(methodOn(ProfessorsApi.class).findById(id)).withSelfRel());
    }

    @Override
    public CollectionModel<EntityModel<Student>> findCourseAttendees(long id, long courseId) {

        Professor professor = timetableFacade.getProfessor(id)
                .orElseThrow(() -> new NotFoundException("Professor with ID " + id + " could not be found"));
        Course course = professor.getCourses()
                .stream()
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Course with ID " + courseId + " could not be found"));
        List<EntityModel<Student>> models = timetableFacade.getCourseAttendees(course, professor)
                .stream()
                .map(studentAssembler::toModel)
                .collect(Collectors.toList());

        return new CollectionModel<>(models,
                linkTo(methodOn(ProfessorsApi.class).findCourseAttendees(id, courseId)).withSelfRel(),
                linkTo(methodOn(ProfessorsApi.class).findById(id)).withRel("professor"),
                linkTo(methodOn(CoursesApi.class).findById(courseId)).withRel("course"));
    }

}
