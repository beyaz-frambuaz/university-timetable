package com.shablii.timetable.api.rest.assemblers;

import com.shablii.timetable.api.StudentsApi;
import com.shablii.timetable.api.rest.StudentsController;
import com.shablii.timetable.model.Student;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class StudentModelAssembler implements RepresentationModelAssembler<Student, EntityModel<Student>> {


    @Override
    public EntityModel<Student> toModel(Student student) {


        StudentsApi controller = methodOn(StudentsController.class);

        return new EntityModel<>(student, linkTo(controller.findById(student.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(IanaLinkRelations.COLLECTION));

    }

}
