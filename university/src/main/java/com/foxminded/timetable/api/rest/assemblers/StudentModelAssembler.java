package com.foxminded.timetable.api.rest.assemblers;

import com.foxminded.timetable.api.StudentsApi;
import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.api.rest.StudentsController;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class StudentModelAssembler
        implements RepresentationModelAssembler<Student, EntityModel<Student>> {


    @Override
    public EntityModel<Student> toModel(Student student) {


        StudentsApi controller =
                methodOn(StudentsController.class);

        return new EntityModel<>(student,
                linkTo(controller.findById(student.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(
                        IanaLinkRelations.COLLECTION));

    }

}
