package com.foxminded.timetable.api.rest.assemblers;

import com.foxminded.timetable.api.ProfessorsApi;
import com.foxminded.timetable.model.Professor;
import com.foxminded.timetable.api.rest.ProfessorsController;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ProfessorModelAssembler implements
        RepresentationModelAssembler<Professor, EntityModel<Professor>> {

    @Override
    public EntityModel<Professor> toModel(Professor professor) {

        ProfessorsApi controller =
                methodOn(ProfessorsController.class);

        return new EntityModel<>(professor,
                linkTo(controller.findById(professor.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(
                        IanaLinkRelations.COLLECTION));

    }

}
