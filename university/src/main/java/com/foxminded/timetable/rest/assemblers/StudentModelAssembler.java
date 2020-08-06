package com.foxminded.timetable.rest.assemblers;

import com.foxminded.timetable.model.Student;
import com.foxminded.timetable.rest.StudentsRestController;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class StudentModelAssembler
        implements RepresentationModelAssembler<Student, EntityModel<Student>> {


    @Override
    public EntityModel<Student> toModel(Student student) {


        StudentsRestController controller =
                methodOn(StudentsRestController.class);

        return new EntityModel<>(student,
                linkTo(controller.findById(student.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(
                        IanaLinkRelations.COLLECTION));

    }

}
