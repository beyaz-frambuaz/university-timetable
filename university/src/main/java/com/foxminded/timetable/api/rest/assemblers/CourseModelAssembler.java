package com.foxminded.timetable.api.rest.assemblers;

import com.foxminded.timetable.api.CoursesApi;
import com.foxminded.timetable.model.Course;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CourseModelAssembler
        implements RepresentationModelAssembler<Course, EntityModel<Course>> {

    @Override
    public EntityModel<Course> toModel(Course course) {

        CoursesApi controller = methodOn(CoursesApi.class);

        return new EntityModel<>(course,
                linkTo(controller.findById(course.getId())).withSelfRel(),
                linkTo(controller.findCourseProfessors(course.getId())).withRel(
                        "professors"), linkTo(controller.findAll()).withRel(
                IanaLinkRelations.COLLECTION));
    }

}
