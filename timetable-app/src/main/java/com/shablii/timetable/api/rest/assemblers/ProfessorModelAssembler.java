package com.shablii.timetable.api.rest.assemblers;

import com.shablii.timetable.api.ProfessorsApi;
import com.shablii.timetable.api.rest.ProfessorsController;
import com.shablii.timetable.model.Professor;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class ProfessorModelAssembler implements RepresentationModelAssembler<Professor, EntityModel<Professor>> {

    @Override
    public EntityModel<Professor> toModel(Professor professor) {

        ProfessorsApi controller = methodOn(ProfessorsController.class);

        return new EntityModel<>(professor, linkTo(controller.findById(professor.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(IanaLinkRelations.COLLECTION));

    }

}
