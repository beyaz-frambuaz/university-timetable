package com.shablii.timetable.api.rest.assemblers;

import com.shablii.timetable.api.AuditoriumsApi;
import com.shablii.timetable.model.Auditorium;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class AuditoriumModelAssembler implements RepresentationModelAssembler<Auditorium, EntityModel<Auditorium>> {

    @Override
    public EntityModel<Auditorium> toModel(Auditorium auditorium) {

        AuditoriumsApi controller = methodOn(AuditoriumsApi.class);

        return new EntityModel<>(auditorium, linkTo(controller.findById(auditorium.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(IanaLinkRelations.COLLECTION));
    }

}
