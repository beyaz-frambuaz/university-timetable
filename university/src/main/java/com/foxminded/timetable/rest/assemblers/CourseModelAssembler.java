package com.foxminded.timetable.rest.assemblers;

import com.foxminded.timetable.model.Course;
import com.foxminded.timetable.rest.CoursesRestController;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CourseModelAssembler
        implements RepresentationModelAssembler<Course, EntityModel<Course>> {

    @Override
    public EntityModel<Course> toModel(Course course) {

        CoursesRestController controller =
                methodOn(CoursesRestController.class);

        return new EntityModel<>(course,
                linkTo(controller.findById(course.getId())).withSelfRel(),
                linkTo(controller.findCourseProfessors(course.getId())).withRel(
                        "professors"), linkTo(controller.findAll()).withRel(
                IanaLinkRelations.COLLECTION));
    }

}
