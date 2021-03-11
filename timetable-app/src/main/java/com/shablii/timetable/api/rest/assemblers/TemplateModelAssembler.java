package com.shablii.timetable.api.rest.assemblers;

import com.shablii.timetable.api.TemplatesApi;
import com.shablii.timetable.api.rest.TemplatesController;
import com.shablii.timetable.model.ScheduleTemplate;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class TemplateModelAssembler
        implements RepresentationModelAssembler<ScheduleTemplate, EntityModel<ScheduleTemplate>> {

    @Override
    public EntityModel<ScheduleTemplate> toModel(ScheduleTemplate template) {

        TemplatesApi controller = methodOn(TemplatesController.class);

        return new EntityModel<>(template, linkTo(controller.findById(template.getId())).withSelfRel(),
                linkTo(controller.findAll()).withRel(IanaLinkRelations.COLLECTION));
    }

}
